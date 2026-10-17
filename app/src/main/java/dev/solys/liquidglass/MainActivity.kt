package dev.solys.liquidglass

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var prefs: SharedPreferences
    private var blurAmount = 18
    private var animationScale = 1.0f
    private var accent = Color.rgb(157, 205, 255)
    private var showingApps = false
    private var dockApps = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("launcher", MODE_PRIVATE)
        blurAmount = prefs.getInt("blur", 18)
        animationScale = prefs.getFloat("animation", 1f)
        accent = prefs.getInt("accent", Color.rgb(157, 205, 255))
        dockApps = prefs.getString("dock", "com.android.chrome,com.google.android.apps.photos,com.android.settings").orEmpty()
            .split(",").filter { it.isNotBlank() }.toMutableList()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.rgb(13, 16, 25)
        window.setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        renderHome()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun glassDrawable(radius: Float = 28f, alpha: Int = 42): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radius.toInt()).toFloat()
            setColor(Color.argb(alpha, 235, 245, 255))
            setStroke(dp(1), Color.argb(90, 255, 255, 255))
        }
    }

    private fun glass(view: View, radius: Float = 28f, alpha: Int = 42) {
        view.background = glassDrawable(radius, alpha)
        if (Build.VERSION.SDK_INT >= 31 && blurAmount > 0) {
            view.setRenderEffect(RenderEffect.createBlurEffect(
                (blurAmount / 3f).coerceAtLeast(1f), (blurAmount / 3f).coerceAtLeast(1f),
                Shader.TileMode.CLAMP
            ))
        }
    }

    private fun text(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
    }

    private fun baseRoot(): FrameLayout {
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(15, 19, 31))
            // Decorative gradient-like ambient lights, rendered as soft translucent blobs.
            addView(View(this@MainActivity).apply {
                background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.rgb(43, 80, 120), Color.rgb(21, 28, 49), Color.rgb(14, 17, 28)))
            }, FrameLayout.LayoutParams(-1, -1))
            addView(View(this@MainActivity).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.argb(75, 78, 150, 235))
                }
            }, FrameLayout.LayoutParams(dp(280), dp(280), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(65); rightMargin = dp(-80)
            })
            addView(View(this@MainActivity).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.argb(60, 155, 95, 230))
                }
            }, FrameLayout.LayoutParams(dp(240), dp(240), Gravity.BOTTOM or Gravity.START).apply {
                bottomMargin = dp(95); leftMargin = dp(-90)
            })
        }
        return root
    }

    private fun renderHome() {
        showingApps = false
        baseRoot()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(42), dp(22), dp(18))
        }
        root.addView(content, FrameLayout.LayoutParams(-1, -1))

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dateBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        dateBlock.addView(text("MONDAY • YOUR SPACE", 11f, Color.LTGRAY, true))
        val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        dateBlock.addView(text(time, 38f, Color.WHITE, true).apply { gravity = Gravity.START })
        top.addView(dateBlock, LinearLayout.LayoutParams(0, -2, 1f))
        val settings = text("⚙", 25f).apply {
            setOnClickListener { showCustomization() }
            contentDescription = "Customize launcher"
        }
        top.addView(settings, LinearLayout.LayoutParams(dp(54), dp(54)))
        content.addView(top)

        val spacer = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 0.7f) }
        content.addView(spacer)

        val greeting = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        greeting.addView(text("Everything, in flow.", 28f, Color.WHITE, true).apply { gravity = Gravity.START })
        greeting.addView(text("Liquid Glass • HyperOS-inspired", 13f, Color.LTGRAY).apply {
            gravity = Gravity.START
            setPadding(0, dp(5), 0, dp(20))
        })
        content.addView(greeting)

        val search = text("⌕     Search apps", 16f, Color.WHITE).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
            glass(this, 24f, 34)
            setOnClickListener { showAppDrawer() }
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(58)))

        val middleSpace = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, 0, 0.35f) }
        content.addView(middleSpace)

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(9), dp(10), dp(9))
            glass(this, 32f, 48)
            elevation = dp(14).toFloat()
        }
        val launchables = getLaunchableApps()
        val chosen = dockApps.mapNotNull { id -> launchables.firstOrNull { it.packageName == id } }.take(5)
        val displayApps = if (chosen.isNotEmpty()) chosen else launchables.take(4)
        displayApps.forEach { app ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(6), dp(3), dp(6), dp(3))
                setOnClickListener { launchApp(app.packageName) }
                setOnLongClickListener { showDockPicker(); true }
            }
            val icon = android.widget.ImageView(this).apply {
                setImageDrawable(app.loadIcon(packageManager))
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            }
            item.addView(icon, LinearLayout.LayoutParams(dp(43), dp(43)))
            dock.addView(item, LinearLayout.LayoutParams(0, dp(56), 1f))
        }
        val all = text("＋", 25f, accent, true).apply {
            setOnClickListener { showAppDrawer() }
            setOnLongClickListener { showDockPicker(); true }
        }
        dock.addView(all, LinearLayout.LayoutParams(dp(42), dp(56)))
        content.addView(dock, LinearLayout.LayoutParams(-1, dp(76)))
        val hint = text("Tap + for apps  •  Long-press dock to edit", 11f, Color.LTGRAY)
        content.addView(hint, LinearLayout.LayoutParams(-1, dp(30)))

        root.alpha = 0f
        root.translationY = dp(12).toFloat()
        root.animate().alpha(1f).translationY(0f).setDuration((420 * animationScale).toLong().coerceAtLeast(120))
            .setInterpolator(DecelerateInterpolator()).start()
        setContentView(root)
    }

    private fun getLaunchableApps(): List<android.content.pm.ApplicationInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }

    private fun showAppDrawer() {
        showingApps = true
        baseRoot()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(30), dp(18), dp(14))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(text("All apps", 27f, Color.WHITE, true).apply { gravity = Gravity.START },
            LinearLayout.LayoutParams(0, dp(56), 1f))
        header.addView(text("✕", 22f).apply { setOnClickListener { renderHome() } },
            LinearLayout.LayoutParams(dp(48), dp(56)))
        layout.addView(header)

        val scroll = ScrollView(this)
        val grid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        val apps = getLaunchableApps()
        apps.forEach { app ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(12), dp(4), dp(12))
                setOnClickListener { launchApp(app.packageName) }
            }
            val icon = android.widget.ImageView(this).apply { setImageDrawable(app.loadIcon(packageManager)) }
            cell.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
            cell.addView(text(app.loadLabel(packageManager).toString(), 10f).apply {
                maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(-1, dp(34)))
            grid.addView(cell, GridLayout.LayoutParams().apply {
                width = 0; height = dp(92); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }
        scroll.addView(grid)
        layout.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(layout, FrameLayout.LayoutParams(-1, -1))
        root.alpha = 0f
        root.scaleX = 0.97f
        root.scaleY = 0.97f
        root.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration((260 * animationScale).toLong().coerceAtLeast(100)).start()
        setContentView(root)
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Toast.makeText(this, "App could not be opened", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showDockPicker() {
        val apps = getLaunchableApps()
        val labels = apps.map { it.loadLabel(packageManager).toString() }.toTypedArray()
        val selected = apps.map { it.packageName }.toMutableList()
        val checked = BooleanArray(apps.size) { apps[it].packageName in dockApps }
        AlertDialog.Builder(this)
            .setTitle("Customize floating dock")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                val id = apps[which].packageName
                if (isChecked && id !in dockApps && dockApps.size < 5) dockApps.add(id)
                else if (!isChecked) dockApps.remove(id)
                else Toast.makeText(this, "Dock supports up to 5 apps", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putString("dock", dockApps.joinToString(",")).apply()
                renderHome()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomization() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(8))
        }
        panel.addView(text("Launcher studio", 24f, Color.WHITE, true))
        panel.addView(text("Make the whole launcher yours", 13f, Color.LTGRAY))
        panel.addView(text("Glass blur", 15f, Color.WHITE, true).apply {
            gravity = Gravity.START
            setPadding(0, dp(20), 0, 0)
        })
        val blurValue = text("$blurAmount / 30", 13f, accent)
        panel.addView(blurValue)
        panel.addView(SeekBar(this).apply {
            max = 30; progress = blurAmount
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    blurAmount = progress; blurValue.text = "$progress / 30"
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        })
        panel.addView(text("Animation speed", 15f, Color.WHITE, true).apply {
            gravity = Gravity.START; setPadding(0, dp(16), 0, 0)
        })
        val speedLabel = text("${animationScale}x", 13f, accent)
        panel.addView(speedLabel)
        panel.addView(SeekBar(this).apply {
            max = 18; progress = ((animationScale - 0.4f) * 10).toInt().coerceIn(0, 18)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    animationScale = 0.4f + progress / 10f
                    speedLabel.text = String.format(java.util.Locale.US, "%.1fx", animationScale)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        })
        panel.addView(text("Accent theme", 15f, Color.WHITE, true).apply {
            gravity = Gravity.START; setPadding(0, dp(16), 0, dp(8))
        })
        val colors = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        val palette = listOf(
            Color.rgb(157,205,255), Color.rgb(170,145,255), Color.rgb(126,230,196),
            Color.rgb(255,176,196), Color.rgb(255,213,135)
        )
        palette.forEach { color ->
            val swatch = View(this).apply {
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(color) }
                setOnClickListener { accent = color }
            }
            colors.addView(swatch, LinearLayout.LayoutParams(dp(34), dp(34)).apply {
                setMargins(dp(7), dp(4), dp(7), dp(4))
            })
        }
        panel.addView(colors)
        AlertDialog.Builder(this)
            .setView(panel)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit().putInt("blur", blurAmount).putFloat("animation", animationScale)
                    .putInt("accent", accent).apply()
                renderHome()
            }
            .setNegativeButton("Cancel") { _, _ -> renderHome() }
            .setNeutralButton("Edit dock") { _, _ -> showDockPicker() }
            .show()
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (showingApps) renderHome() else super.onBackPressed()
    }
}
