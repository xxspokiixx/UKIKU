package knf.kuma.search

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import knf.kuma.R
import knf.kuma.animeinfo.ActivityAnime
import knf.kuma.commons.PatternUtil
import knf.kuma.commons.PicassoSingle
import knf.kuma.pojos.AnimeObject
import kotlinx.android.synthetic.main.item_dir.view.*

internal class GenreAdapter(private val activity: Activity) : PagedListAdapter<AnimeObject, GenreAdapter.ItemHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dir, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val animeObject = getItem(position)
        if (animeObject != null) {
            PicassoSingle[activity].load(PatternUtil.getCover(animeObject.aid!!)).into(holder.imageView)
            holder.textView.text = animeObject.name
            holder.cardView.setOnClickListener { ActivityAnime.open(activity, animeObject, holder.imageView, false, true) }
        }
    }

    internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.card
        val imageView: ImageView = itemView.img
        val textView: TextView = itemView.title
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<AnimeObject> = object : DiffUtil.ItemCallback<AnimeObject>() {
            override fun areItemsTheSame(oldItem: AnimeObject, newItem: AnimeObject): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: AnimeObject, newItem: AnimeObject): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}
