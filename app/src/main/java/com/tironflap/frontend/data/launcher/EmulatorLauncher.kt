package com.tironflap.frontend.data.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.tironflap.frontend.data.model.Game
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmulatorLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class EmulatorTarget(
        val packageName: String,
        val label: String
    )

    /** Known emulator packages per system (common Android ports). */
    private val emulatorsBySystem: Map<String, List<EmulatorTarget>> = mapOf(
        "nes" to listOf(
            EmulatorTarget("com.fms.emuid", "EmuNes"),
            EmulatorTarget("com.jacksonblaze.nes", "Nostalgia.NES"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "snes" to listOf(
            EmulatorTarget("com.explusalpha.Snes9xPlus", "Snes9x EX+"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "n64" to listOf(
            EmulatorTarget("org.mupen64plusae.v3.fzurita", "M64Plus FZ"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "gb" to listOf(
            EmulatorTarget("com.github.stenzek.mgsm", "My OldBoy"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "gbc" to listOf(
            EmulatorTarget("com.github.stenzek.mgsm", "My OldBoy"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "gba" to listOf(
            EmulatorTarget("com.fastemulator.gba", "My Boy!"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "nds" to listOf(
            EmulatorTarget("com.dsemu.drastic", "DraStic"),
            EmulatorTarget("com.github.stenzek.melonDS", "melonDS"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "3ds" to listOf(
            EmulatorTarget("org.citra.citra_emu", "Citra"),
            EmulatorTarget("io.github.lime3ds.android", "Lime3DS"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "gc" to listOf(
            EmulatorTarget("org.dolphinemu.dolphinemu", "Dolphin"),
            EmulatorTarget("org.mm.jbmm", "Dolphin MMJR")
        ),
        "wii" to listOf(
            EmulatorTarget("org.dolphinemu.dolphinemu", "Dolphin"),
            EmulatorTarget("org.mm.jbmm", "Dolphin MMJR")
        ),
        "wiiu" to listOf(
            EmulatorTarget("info.cemu.cemu", "Cemu"),
            EmulatorTarget("com.github.stenzek.cemu", "Cemu")
        ),
        "switch" to listOf(
            EmulatorTarget("org.yuzu.yuzu_emu", "Yuzu"),
            EmulatorTarget("org.ryujinx.android", "Ryujinx"),
            EmulatorTarget("com.github.stenzek.suyu", "Suyu")
        ),
        "psx" to listOf(
            EmulatorTarget("com.github.stenzek.duckstation", "DuckStation"),
            EmulatorTarget("com.epsxe.ePSXe", "ePSXe"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "ps2" to listOf(
            EmulatorTarget("xyz.aethersx2.android", "AetherSX2"),
            EmulatorTarget("com.github.stenzek.pcsx2", "PCSX2")
        ),
        "psp" to listOf(
            EmulatorTarget("org.ppsspp.ppsspp", "PPSSPP"),
            EmulatorTarget("org.ppsspp.ppssppgold", "PPSSPP Gold")
        ),
        "dreamcast" to listOf(
            EmulatorTarget("com.reicast.emulator", "Redream"),
            EmulatorTarget("com.flycast.emulator", "Flycast"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "genesis" to listOf(
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "arcade" to listOf(
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        )
    )

    fun findInstalledEmulators(systemId: String): List<EmulatorTarget> {
        val pm = context.packageManager
        val candidates = emulatorsBySystem[systemId].orEmpty()
        return candidates.filter { target ->
            try {
                pm.getPackageInfo(target.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    /**
     * Launch [game] with the first installed emulator for its system.
     * Uses ACTION_VIEW with the content URI so modern emulators can open it.
     */
    fun launch(game: Game): Boolean {
        val installed = findInstalledEmulators(game.systemId)
        if (installed.isEmpty()) {
            Toast.makeText(
                context,
                "No emulator installed for ${game.systemId.uppercase()}. Install DuckStation / PPSSPP / Dolphin / RetroArch etc.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val target = installed.first()
        val uri = Uri.parse(game.path)

        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                setPackage(target.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Grant read permission to the emulator package
            try {
                context.grantUriPermission(
                    target.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback: open chooser without forced package
            try {
                val fallback = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/octet-stream")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(fallback, "Open ${game.name} with")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                true
            } catch (e2: Exception) {
                Toast.makeText(
                    context,
                    "Could not launch: ${e2.message}",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }
}
