package knf.kuma.tv.anime

import android.view.ViewGroup

import androidx.leanback.widget.Presenter
import knf.kuma.database.CacheDB
import knf.kuma.pojos.RecentObject
import knf.kuma.tv.cards.RecentsCardView
import knf.kuma.tv.details.TVAnimesDetails

class RecentsPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        return Presenter.ViewHolder(RecentsCardView(parent.context))
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        (viewHolder.view as RecentsCardView).bind(item as RecentObject)
        viewHolder.view.setOnLongClickListener { v ->
            val animeObject = CacheDB.INSTANCE.animeDAO().getByAid(item.aid!!)
            if (animeObject != null)
                TVAnimesDetails.start(v.context, animeObject.link!!)
            true
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {

    }
}