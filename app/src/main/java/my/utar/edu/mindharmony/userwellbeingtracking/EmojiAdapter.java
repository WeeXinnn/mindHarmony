package my.utar.edu.mindharmony.userwellbeingtracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import my.utar.edu.mindharmony.R;

public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

    public interface Callback { void onSelect(EmojiItem e); }

    private final List<EmojiItem> items;
    private final Callback cb;

    public EmojiAdapter(List<EmojiItem> items, Callback cb) {
        this.items = items;
        this.cb = cb;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ViewHolder(View v) {
            super(v);
            img = v.findViewById(R.id.emojiImg);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emoji, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int pos) {
        EmojiItem e = items.get(pos);
        vh.img.setImageResource(e.drawableRes);
        vh.img.setContentDescription(e.name);
        vh.img.setOnClickListener(v -> cb.onSelect(e));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
