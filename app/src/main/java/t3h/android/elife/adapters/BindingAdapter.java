package t3h.android.elife.adapters;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import t3h.android.elife.R;

public class BindingAdapter {
    @androidx.databinding.BindingAdapter(value = {"url"})
    public static void bindImageView(ImageView view, String url) {
        Glide.with(view)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .into(view);
    }
}
