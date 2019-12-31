package ai.blockwell.qrdemo.trainer.suggestions

import ai.blockwell.qrdemo.R
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_suggestion.view.*
import kotlinx.android.synthetic.main.view_suggestions_list.view.*
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.textColorResource

class SuggestionsListView : FrameLayout {
    @JvmOverloads
    constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    private val adapter = SuggestionsAdapter()

    init {
        context.layoutInflater.inflate(R.layout.view_suggestions_list, this, true)

        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context)

        val decorator = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        decorator.setDrawable(getDrawable(context, R.drawable.divider)!!)
        recycler.addItemDecoration(decorator)
    }

    fun loading() {
        recycler.visibility = View.INVISIBLE
        progress.visibility = View.VISIBLE
    }

    fun setSuggestions(suggestions: List<Suggestion>) {
        progress.visibility = View.GONE
        recycler.visibility = View.VISIBLE

        adapter.setSuggestions(suggestions)
    }

    fun setClickListener(block: (suggestion: Suggestion) -> Unit) {
        adapter.clickListener = block
    }

    class SuggestionsAdapter : RecyclerView.Adapter<SuggestionHolder>() {

        var clickListener: ((suggestion: Suggestion) -> Unit)? = null

        private var items = listOf<Suggestion>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
                = SuggestionHolder(parent.context.layoutInflater.inflate(R.layout.item_suggestion, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: SuggestionHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
            clickListener?.let {
                holder.view.setOnClickListener { it(items[position]) }
            }
        }

        fun setSuggestions(suggestions: List<Suggestion>) {
            items = suggestions
            notifyDataSetChanged()
        }
    }

    class SuggestionHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(suggestion: Suggestion) {
            view.text.text = suggestion.suggestion
            view.votes.text = suggestion.votes
            view.number.text = view.context.getString(R.string.suggestion_num, suggestion.id)

            if (suggestion.tag.isNotEmpty()) {
                view.tagView.text = suggestion.tag
                view.tagView.visibility = View.VISIBLE
            } else {
                view.tagView.visibility = View.GONE
            }

            if (suggestion.proposal) {
                view.proposal.visibility = View.VISIBLE
                view.votes.textColorResource = R.color.proposal
            } else {
                view.proposal.visibility = View.GONE
                view.votes.textColorResource = R.color.colorTextEmphasis
            }
        }
    }
}
