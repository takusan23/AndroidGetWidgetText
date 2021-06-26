package io.github.takusan23.androidgetwidgettext

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.postDelayed
import io.github.takusan23.androidgetwidgettext.databinding.ActivityMainBinding

/**
 * 注意点としては、AppCompatActivity()ではなくActivity()を継承するところです
 * */
class MainActivity : Activity() {

    private val viewBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(this) }
    private val appWidgetHost by lazy { AppWidgetHost(this, R.string.app_name) }
    private val REQUEST_PICK_WIDGET = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        startActivityForResult(intent, REQUEST_PICK_WIDGET)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_WIDGET && data != null) {
                val extras = data.extras!!
                val appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                createWidget(appWidgetId)
            }
        }
    }

    private fun createWidget(appWidgetId: Int) {
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
        hostView.setAppWidget(appWidgetId, appWidgetInfo)
        viewBinding.activityMainWidgetHostParentFramelayout.addView(hostView)
        // 遅延実行
        viewBinding.root.postDelayed(500) {
            getAllView(hostView.rootView as ViewGroup)
        }
    }

    /** 再帰的にViewを取得してすべてのViewを取得 */
    private fun getAllView(viewGroup: ViewGroup) {
        val resultViewList = arrayListOf<View>()

        fun findViewGroup(viewGroup: ViewGroup) {
            viewGroup.children.forEach { view ->
                resultViewList.add(view)
                if (view is ViewGroup) {
                    findViewGroup(view)
                }
            }
        }

        findViewGroup(viewGroup)

        // テキストのみを取り出す
        viewBinding.activityMainAllWidgetText.text = resultViewList
            .filterIsInstance<TextView>()
            .map { textView -> "id = ${textView.id} \t text = ${textView.text}" }
            .joinToString(separator = "\n")
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

}