package knf.kuma.animeinfo.viewholders

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener
import com.michaelflisar.dragselectrecyclerview.DragSelectionProcessor
import knf.kuma.animeinfo.AnimeChaptersAdapter
import knf.kuma.animeinfo.BottomActionsDialog
import knf.kuma.commons.PatternUtil
import knf.kuma.commons.safeDismiss
import knf.kuma.commons.showSnackbar
import knf.kuma.database.CacheDB
import knf.kuma.download.FileAccessHelper
import knf.kuma.pojos.AnimeObject
import kotlinx.android.synthetic.main.recycler_chapters.view.*
import org.jetbrains.anko.doAsync
import java.util.*

class AnimeChaptersHolder(view: View, private val fragmentManager: FragmentManager, private val callback: ChapHolderCallback) {
    val recyclerView: RecyclerView = view.recycler
    private val manager: LinearLayoutManager = LinearLayoutManager(view.context)
    private var chapters: MutableList<AnimeObject.WebInfo.AnimeChapter> = ArrayList()
    var adapter: AnimeChaptersAdapter? = null
        private set
    private val touchListener: DragSelectTouchListener

    init {
        manager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = manager
        touchListener = DragSelectTouchListener()
                .withSelectListener(DragSelectionProcessor(object : DragSelectionProcessor.ISelectionHandler {
                    override fun getSelection(): Set<Int> {
                        return adapter!!.selection
                    }

                    override fun isSelected(i: Int): Boolean {
                        return adapter!!.selection.contains(i)
                    }

                    override fun updateSelection(i: Int, i1: Int, b: Boolean, b1: Boolean) {
                        adapter!!.selectRange(i, i1, b)
                    }
                }).withStartFinishedListener(object : DragSelectionProcessor.ISelectionStartFinishedListener {
                    override fun onSelectionStarted(i: Int, b: Boolean) {

                    }

                    override fun onSelectionFinished(i: Int) {
                        BottomActionsDialog.newInstance(object : BottomActionsDialog.ActionsCallback {
                            override fun onSelect(state: Int) {
                                try {
                                    val snackbar = recyclerView.showSnackbar("Procesando...", duration = Snackbar.LENGTH_INDEFINITE)
                                    when (state) {
                                        BottomActionsDialog.STATE_SEEN -> doAsync {
                                            val dao = CacheDB.INSTANCE.chaptersDAO()
                                            for (i13 in ArrayList(adapter!!.selection)) {
                                                dao.addChapter(chapters[i13])
                                            }
                                            val seeingDAO = CacheDB.INSTANCE.seeingDAO()
                                            val seeingObject = seeingDAO.getByAid(chapters[0].aid)
                                            if (seeingObject != null) {
                                                seeingObject.chapter = chapters[0].number
                                                seeingDAO.update(seeingObject)
                                            }
                                            recyclerView.post { adapter!!.deselectAll() }
                                            snackbar.safeDismiss()
                                        }
                                        BottomActionsDialog.STATE_UNSEEN -> doAsync {
                                            try {
                                                val dao = CacheDB.INSTANCE.chaptersDAO()
                                                for (i12 in ArrayList(adapter!!.selection)) {
                                                    dao.deleteChapter(chapters[i12])
                                                }
                                                val seeingDAO = CacheDB.INSTANCE.seeingDAO()
                                                val seeingObject = seeingDAO.getByAid(chapters[0].aid)
                                                if (seeingObject != null) {
                                                    seeingObject.chapter = chapters[0].number
                                                    seeingDAO.update(seeingObject)
                                                }
                                                recyclerView.post { adapter!!.deselectAll() }
                                                snackbar.safeDismiss()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                snackbar.safeDismiss()
                                            }
                                        }
                                        BottomActionsDialog.STATE_IMPORT_MULTIPLE -> doAsync {
                                            try {
                                                val cChapters = ArrayList<AnimeObject.WebInfo.AnimeChapter>()
                                                val downloadsDAO = CacheDB.INSTANCE.downloadsDAO()
                                                for (i13 in ArrayList(adapter!!.selection)) {
                                                    val chapter = chapters[i13]
                                                    val file = FileAccessHelper.INSTANCE.getFile(chapter.fileName)
                                                    val downloadObject = downloadsDAO.getByEid(chapter.eid)
                                                    if (!file.exists() && (downloadObject == null || !downloadObject.isDownloading))
                                                        cChapters.add(chapter)
                                                }
                                                callback.onImportMultiple(cChapters)
                                                recyclerView.post { adapter!!.deselectAll() }
                                                snackbar.safeDismiss()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                snackbar.safeDismiss()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    //
                                }

                            }

                            override fun onDismiss() {
                                adapter!!.deselectAll()
                            }
                        }).safeShow(fragmentManager, "actions_dialog")
                    }
                }).withMode(DragSelectionProcessor.Mode.Simple))
                .withMaxScrollDistance(32)
    }

    fun setAdapter(fragment: Fragment, chapters: MutableList<AnimeObject.WebInfo.AnimeChapter>) {
        this.chapters = chapters
        this.adapter = AnimeChaptersAdapter(fragment, recyclerView, chapters, touchListener)
        recyclerView.post {
            recyclerView.adapter = adapter
            recyclerView.addOnItemTouchListener(touchListener)
        }
    }

    fun refresh() {
        if (adapter != null)
            recyclerView.post { adapter!!.notifyDataSetChanged() }
    }

    fun goToChapter() {
        if (chapters.isNotEmpty()) {
            val chapter = CacheDB.INSTANCE.chaptersDAO().getLast(PatternUtil.getEids(chapters))
            if (chapter != null) {
                val position = chapters.indexOf(chapter)
                if (position >= 0)
                    manager.scrollToPositionWithOffset(position, 150)
            }
        }
    }

    fun smoothGoToChapter() {
        if (chapters.isNotEmpty()) {
            val chapter = CacheDB.INSTANCE.chaptersDAO().getLast(PatternUtil.getEids(chapters))
            if (chapter != null) {
                val position = chapters.indexOf(chapter)
                if (position >= 0)
                    recyclerView.post { manager.smoothScrollToPosition(recyclerView, null, position) }
            }
        }
    }

    interface ChapHolderCallback {
        fun onImportMultiple(chapters: MutableList<AnimeObject.WebInfo.AnimeChapter>)
    }
}