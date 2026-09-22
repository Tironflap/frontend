package com.tironflap.frontend.data.launcher

import android.content.ActivityNotFoundException
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
        val label: String,
        /** Optional activity class if needed */
        val activity: String? = null
    )

    private val emulatorsBySystem: Map<String, List<EmulatorTarget>> = mapOf(
        "nes" to listOf(
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.jacksonblaze.nes", "Nostalgia.NES")
        ),
        "snes" to listOf(
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.explusalpha.Snes9xPlus", "Snes9x EX+")
        ),
        "n64" to listOf(
            EmulatorTarget("org.mupen64plusae.v3.fzurita", "M64Plus FZ"),
            EmulatorTarget("org.mupen64plusae.v3.fzurita.pro", "M64Plus FZ Pro"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "gb" to listOf(
            EmulatorTarget("com.fastemulator.gb", "My OldBoy!"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "gbc" to listOf(
            EmulatorTarget("com.fastemulator.gb", "My OldBoy!"),
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
            EmulatorTarget("me.magnum.melonds", "melonDS"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "3ds" to listOf(
            EmulatorTarget("org.citra.citra_emu", "Citra"),
            EmulatorTarget("io.github.lime3ds.android", "Lime3DS"),
            EmulatorTarget("org.citra.citra_emu.canary", "Citra Canary"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "gc" to listOf(
            EmulatorTarget("org.dolphinemu.dolphinemu", "Dolphin"),
            EmulatorTarget("org.dolphinemu.mmjr", "Dolphin MMJR"),
            EmulatorTarget("org.mm.jbmm", "Dolphin MMJR")
        ),
        "wii" to listOf(
            EmulatorTarget("org.dolphinemu.dolphinemu", "Dolphin"),
            EmulatorTarget("org.dolphinemu.mmjr", "Dolphin MMJR")
        ),
        "wiiu" to listOf(
            EmulatorTarget("info.cemu.cemu", "Cemu"),
            EmulatorTarget("com.github.stenzek.cemu", "Cemu")
        ),
        "switch" to listOf(
            EmulatorTarget("org.yuzu.yuzu_emu", "Yuzu"),
            EmulatorTarget("org.yuzu.yuzu_emu.ea", "Yuzu EA"),
            EmulatorTarget("com.github.stenzek.suyu", "Suyu")
        ),
        "psx" to listOf(
            EmulatorTarget("com.github.stenzek.duckstation", "DuckStation"),
            EmulatorTarget("com.github.stenzek.duckstation.earlyaccess", "DuckStation EA"),
            EmulatorTarget("com.epsxe.ePSXe", "ePSXe"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "ps2" to listOf(
            EmulatorTarget("xyz.aethersx2.android", "AetherSX2"),
            EmulatorTarget("com.aethersx2", "AetherSX2"),
            EmulatorTarget("xyz.aethersx2.android.classic", "AetherSX2 Classic"),
            EmulatorTarget("com.nethersx2.android", "NetherSX2"),
            EmulatorTarget("xyz.nethersx2.android", "NetherSX2"),
            EmulatorTarget("com.github.stenzek.pcsx2", "PCSX2")
        ),
        "psp" to listOf(
            EmulatorTarget("org.ppsspp.ppsspp", "PPSSPP"),
            EmulatorTarget("org.ppsspp.ppssppgold", "PPSSPP Gold"),
            EmulatorTarget("org.ppsspp.ppssppvr", "PPSSPP VR")
        ),
        "dreamcast" to listOf(
            EmulatorTarget("com.reicast.emulator", "Reicast"),
            EmulatorTarget("com.flycast.emulator", "Flycast"),
            EmulatorTarget("com.retroarch", "RetroArch"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
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
        return emulatorsBySystem[systemId].orEmpty().filter { target ->
            isPackageInstalled(pm, target.packageName)
        }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Launch game with multiple strategies so at least one works on real devices.
     */
    fun launch(game: Game): Boolean {
        val uri = Uri.parse(game.path)
        val installed = findInstalledEmulators(game.systemId)

        if (installed.isEmpty()) {
            // Last resort: system chooser for any app that claims this type
            return launchWithChooser(uri, game.name, game.systemId)
        }

        // Try each installed emulator in order
        for (target in installed) {
            if (tryLaunch(uri, target, game)) {
                Toast.makeText(context, "Launching with ${target.label}", Toast.LENGTH_SHORT).show()
                return true
            }
        }

        // Fallback chooser among anything that can open the file
        return launchWithChooser(uri, game.name, game.systemId)
    }

    private fun tryLaunch(uri: Uri, target: EmulatorTarget, game: Game): Boolean {
        // Strategy 1: ACTION_VIEW + package + content URI
        if (startViewIntent(uri, target.packageName, "application/octet-stream")) return true
        if (startViewIntent(uri, target.packageName, "*/*")) return true

        // Strategy 2: ACTION_VIEW without explicit MIME
        if (startViewIntent(uri, target.packageName, null)) return true

        // Strategy 3: RetroArch-style extras (ROM path)
        if (target.packageName.contains("retroarch")) {
            if (startRetroArch(uri, target.packageName, game.systemId)) return true
        }

        // Strategy 4: generic MAIN + data
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(target.packageName)
            if (intent != null) {
                intent.action = Intent.ACTION_VIEW
                intent.data = uri
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                grantUri(target.packageName, uri)
                context.startActivity(intent)
                return true
            }
        } catch (_: Exception) {
        }

        return false
    }

    private fun startViewIntent(uri: Uri, packageName: String, mime: String?): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (mime != null) setDataAndType(uri, mime) else data = uri
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            grantUri(packageName, uri)
            // Check something can handle it
            if (intent.resolveActivity(context.packageManager) == null) return false
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun startRetroArch(uri: Uri, packageName: String, systemId: String): Boolean {
        return try {
            val core = retroArchCore(systemId) ?: return false
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setPackage(packageName)
                data = uri
                putExtra("ROM", uri.toString())
                putExtra("CONFIGFILE", "")
                putExtra("LIBRETRO", core)
                putExtra("QUITFOCUS", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            grantUri(packageName, uri)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun retroArchCore(systemId: String): String? = when (systemId) {
        "nes" -> "nes"
        "snes" -> "snes9x"
        "n64" -> "mupen64plus_next"
        "gb", "gbc" -> "gambatte"
        "gba" -> "mgba"
        "nds" -> "melonds"
        "psx" -> "pcsx_rearmed"
        "psp" -> "ppsspp"
        "genesis" -> "genesis_plus_gx"
        "arcade" -> "fbneo"
        else -> null
    }

    private fun launchWithChooser(uri: Uri, gameName: String, systemId: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Open $gameName with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(
                context,
                "Pick an emulator for ${systemId.uppercase()} (install AetherSX2 / DuckStation / PPSSPP if none listed)",
                Toast.LENGTH_LONG
            ).show()
            true
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Cannot launch: ${e.message}. Install an emulator for ${systemId.uppercase()}.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    private fun grantUri(packageName: String, uri: Uri) {
        try {
            context.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
    }
}
