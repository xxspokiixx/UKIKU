package knf.kuma.download

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import knf.kuma.commons.EAHelper
import knf.kuma.commons.PatternUtil
import knf.kuma.commons.safeDismiss
import knf.kuma.commons.safeShow
import knf.kuma.pojos.AnimeObject
import knf.kuma.pojos.DownloadObject
import knf.kuma.pojos.NotificationObj
import knf.kuma.videoservers.ServersFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import java.util.regex.Pattern

class DownloadDialogActivity : AppCompatActivity() {

    private var downloadObject: DownloadObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(EAHelper.getThemeDialog(this))
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setFinishOnTouchOutside(false)
        val dialog = MaterialDialog(this).safeShow {
            message(text = "Obteniendo informacion...")
            cancelable(false)
            cancelOnTouchOutside(false)
        }
        doAsync {
            try {
                val document = Jsoup.connect(intent.dataString).get()
                val name = PatternUtil.fromHtml(document.select("nav.Brdcrmb.fa-home a[href^=/anime/]").first().text())
                var aid: String? = null
                val eid = extract(intent.dataString, "^.*/(\\d+)/.*$")
                var num: String? = null
                val matcher = Pattern.compile("var (.*) = (\\d+);").matcher(document.html())
                while (matcher.find()) {
                    when (matcher.group(1)) {
                        "anime_id" -> aid = matcher.group(2)
                        "episode_number" -> num = matcher.group(2)
                    }
                }
                val chapter = AnimeObject.WebInfo.AnimeChapter(Integer.parseInt(aid!!), "Episodio " + num!!, eid, intent.dataString!!, name, aid)
                downloadObject = DownloadObject.fromChapter(chapter, false)
                launch(UI) {
                    dialog.safeDismiss()
                    try {
                        showSelectDialog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }

    }

    private fun showSelectDialog() {
        MaterialDialog(this).safeShow {
            listItems(items = listOf("Descarga", "Streaming")) { _, index, _ ->
                ServersFactory.start(this@DownloadDialogActivity, intent.dataString!!, downloadObject!!, index == 1, object : ServersFactory.ServersInterface {
                    override fun onFinish(started: Boolean, success: Boolean) {
                        if (success)
                            removeNotification()
                        finish()
                    }

                    override fun onCast(url: String?) {

                    }

                    override fun onProgressIndicator(boolean: Boolean) {

                    }

                    override fun getView(): View? {
                        return null
                    }
                })
            }
            setOnCancelListener { finish() }
        }
    }

    private fun extract(st: String?, regex: String): String {
        val matcher = Pattern.compile(regex).matcher(st)
        matcher.find()
        return matcher.group(1)
    }

    private fun removeNotification() {
        if (intent.getBooleanExtra("notification", false))
            sendBroadcast(NotificationObj.fromIntent(intent).getBroadcast(this))
    }
}
