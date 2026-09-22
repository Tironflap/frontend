package com.tironflap.frontend.data.launcher

import android.content.ActivityNotFoundException
import android.content.ComponentName
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
        val activity: String? = null
    )

    private val emulatorsBySystem: Map<String, List<EmulatorTarget>> = mapOf(
        "nes" to listOf(
            EmulatorTarget("com.jacksonblaze.nes", "Nostalgia.NES"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "snes" to listOf(
            EmulatorTarget("com.explusalpha.Snes9xPlus", "Snes9x EX+"),
            EmulatorTarget("com.explusalpha.Snes9xPlus", "Snes9x EX+", "com.explusalpha.Snes9xPlus.Snes9xActivity"),
            EmulatorTarget("com.explusalpha.Snes9xPlus", "Snes9x EX+", "com.explusalpha.common.EmulatorActivity"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "n64" to listOf(
            EmulatorTarget("org.mupen64plusae.v3.fzurita", "M64Plus FZ"),
            EmulatorTarget("org.mupen64plusae.v3.fzurita.pro", "M64Plus FZ Pro"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "gb" to listOf(
            EmulatorTarget("com.fastemulator.gb", "My OldBoy!"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "gbc" to listOf(
            EmulatorTarget("com.fastemulator.gb", "My OldBoy!"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "gba" to listOf(
            EmulatorTarget("com.fastemulator.gba", "My Boy!"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "nds" to listOf(
            EmulatorTarget("com.dsemu.drastic", "DraStic"),
            EmulatorTarget("me.magnum.melonds", "melonDS"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "3ds" to listOf(
            EmulatorTarget("org.citra.citra_emu", "Citra"),
            EmulatorTarget("io.github.lime3ds.android", "Lime3DS"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
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
            EmulatorTarget("info.cemu.cemu", "Cemu")
        ),
        "psx" to listOf(
            EmulatorTarget("com.github.stenzek.duckstation", "DuckStation"),
            EmulatorTarget("com.github.stenzek.duckstation.earlyaccess", "DuckStation EA"),
            EmulatorTarget("com.epsxe.ePSXe", "ePSXe"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "ps2" to listOf(
            EmulatorTarget("xyz.aethersx2.android", "AetherSX2"),
            EmulatorTarget("com.aethersx2", "AetherSX2"),
            EmulatorTarget("com.nethersx2.android", "NetherSX2"),
            EmulatorTarget("xyz.nethersx2.android", "NetherSX2")
        ),
        "psp" to listOf(
            EmulatorTarget("org.ppsspp.ppsspp", "PPSSPP", "org.ppsspp.ppsspp.PpssppActivity"),
            EmulatorTarget("org.ppsspp.ppssppgold", "PPSSPP Gold", "org.ppsspp.ppsspp.PpssppActivity"),
            EmulatorTarget("org.ppsspp.ppsspp", "PPSSPP"),
            EmulatorTarget("org.ppsspp.ppssppgold", "PPSSPP Gold")
        ),
        "dreamcast" to listOf(
            EmulatorTarget("com.flycast.emulator", "Flycast"),
            EmulatorTarget("com.reicast.emulator", "Reicast"),
            EmulatorTarget("com.retroarch.aarch64", "RetroArch")
        ),
        "genesis" to listOf(
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        ),
        "arcade" to listOf(
            EmulatorTarget("com.retroarch.aarch64", "RetroArch"),
            EmulatorTarget("com.retroarch", "RetroArch")
        )
    )

    fun findInstalledEmulators(systemId: String): List<EmulatorTarget> {
        val pm = context.packageManager
        // Dedupe by package while keeping order
        val seen = mutableSetOf<String>()
        return emulatorsBySystem[systemId].orEmpty().filter { target ->
            if (target.packageName in seen) return@filter false
            val ok = isPackageInstalled(pm, target.packageName)
            if (ok) seen += target.packageName
            ok
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

    fun launch(game: Game): Boolean {
        val uri = Uri.parse(game.path)
        val installed = findInstalledEmulators(game.systemId)

        if (installed.isNotEmpty()) {
            for (target in installed) {
                // Try every known activity variant for this package
                val variants = emulatorsBySystem[game.systemId].orEmpty()
                    .filter { it.packageName == target.packageName }
                for (variant in variants) {
                    if (tryDirectLaunch(uri, variant)) {
                        Toast.makeText(context, "Opening in ${target.label}", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                if (tryDirectLaunch(uri, target)) {
                    Toast.makeText(context, "Opening in ${target.label}", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            Toast.makeText(
                context,
                "Found ${installed.first().label} but could not open the ROM. Grant storage access inside that emulator once.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val hint = when (game.systemId) {
            "psp" -> "Install PPSSPP"
            "snes" -> "Install Snes9x EX+"
            "psx" -> "Install DuckStation"
            "ps2" -> "Install AetherSX2"
            "gc", "wii" -> "Install Dolphin"
            "nds" -> "Install DraStic"
            "gba" -> "Install My Boy!"
            "nes" -> "Install a NES emulator or RetroArch"
            else -> "Install an emulator for ${game.systemId.uppercase()}"
        }
        Toast.makeText(
            context,
            "No emulator for ${game.systemId.uppercase()}. $hint",
            Toast.LENGTH_LONG
        ).show()
        return false
    }

    private fun tryDirectLaunch(uri: Uri, target: EmulatorTarget): Boolean {
        if (target.activity != null) {
            if (startExplicit(uri, target.packageName, target.activity)) return true
        }
        if (startView(uri, target.packageName, "application/octet-stream")) return true
        if (startView(uri, target.packageName, "*/*")) return true
        if (startView(uri, target.packageName, null)) return true
        if (startViaLaunchIntent(uri, target.packageName)) return true
        return false
    }

    private fun startExplicit(uri: Uri, packageName: String, activity: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                component = ComponentName(packageName, activity)
                setDataAndType(uri, "*/*")
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

    private fun startView(uri: Uri, packageName: String, mime: String?): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (mime != null) setDataAndType(uri, mime) else data = uri
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            grantUri(packageName, uri)
            if (intent.resolveActivity(context.packageManager) == null) return false
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun startViaLaunchIntent(uri: Uri, packageName: String): Boolean {
        return try {
            val launch = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
            launch.action = Intent.ACTION_VIEW
            launch.setDataAndType(uri, "*/*")
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launch.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUri(packageName, uri)
            context.startActivity(launch)
            true
        } catch (_: Exception) {
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
