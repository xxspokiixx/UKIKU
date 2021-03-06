package knf.kuma.seeing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import knf.kuma.R
import knf.kuma.database.CacheDB
import knf.kuma.pojos.SeeingObject
import kotlinx.android.synthetic.main.fragment_seeing.*
import xdroid.toaster.Toaster

class SeeingFragment : Fragment() {

    var clickCount = 0

    private val adapter: SeeingAdapter by lazy { SeeingAdapter(activity!!, arguments?.getInt("state", 0) == 0) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        liveData.observe(this, Observer {
            error.visibility = if (it?.isEmpty() == true) View.VISIBLE else View.GONE
            adapter.update(it)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_seeing, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (arguments?.getInt("state", 0)) {
            1 -> {
                error_text.text = "No estas viendo ningun anime"
                error_img.setImageResource(R.drawable.ic_watching)
            }
            2 -> {
                error_text.text = "No consideras ningun anime"
                error_img.setImageResource(R.drawable.ic_considering)
            }
            3 -> {
                error_text.text = "No has terminado ningun anime"
                error_img.setImageResource(R.drawable.ic_completed)
            }
            4 -> {
                error_text.text = "No has dropeado ningun anime"
                error_img.setImageResource(R.drawable.ic_droped)
            }
            else -> error_text.text = "No has marcado ningun anime"
        }
        recycler.adapter = adapter
    }

    val liveData: LiveData<List<SeeingObject>>
        get() {
            return if (arguments?.getInt("state", 0) ?: 0 == 0)
                CacheDB.INSTANCE.seeingDAO().all
            else
                CacheDB.INSTANCE.seeingDAO().getLiveByState(arguments?.getInt("state", 0) ?: 0)
        }

    val title: String
        get() {
            return when (arguments?.getInt("state", 0)) {
                1 -> "Viendo"
                2 -> "Considerando"
                3 -> "Completado"
                4 -> "Dropeado"
                else -> "Todos"
            }
        }

    fun onSelected() {
        clickCount++
        if (clickCount == 3 && adapter.list.isNotEmpty()) {
            Toaster.toast("${adapter.list.size} anime" + if (adapter.list.size > 1) "s" else "")
            clickCount = 0
        }
    }

    companion object {
        operator fun get(state: Int): SeeingFragment {
            val emissionFragment = SeeingFragment()
            val bundle = Bundle()
            bundle.putInt("state", state)
            emissionFragment.arguments = bundle
            return emissionFragment
        }
    }
}