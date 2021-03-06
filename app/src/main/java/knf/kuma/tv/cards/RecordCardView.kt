package knf.kuma.tv.cards

import android.content.Context
import android.widget.ImageView
import knf.kuma.R
import knf.kuma.commons.PatternUtil
import knf.kuma.commons.PicassoSingle
import knf.kuma.pojos.RecordObject
import knf.kuma.tv.BindableCardView
import kotlinx.android.synthetic.main.item_tv_card_chapter.view.*

class RecordCardView(context: Context) : BindableCardView<RecordObject>(context) {

    override val imageView: ImageView
        get() = img
    override val layoutResource: Int
        get() = R.layout.item_tv_card_chapter

    override fun bind(data: RecordObject) {
        PicassoSingle[context].load(PatternUtil.getCover(data.aid!!)).into(img)
        title!!.text = data.name
        chapter!!.text = data.chapter
    }
}
