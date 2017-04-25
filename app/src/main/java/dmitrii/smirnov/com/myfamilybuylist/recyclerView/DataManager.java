package dmitrii.smirnov.com.myfamilybuylist.recyclerView;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import dmitrii.smirnov.com.myfamilybuylist.R;
import dmitrii.smirnov.com.myfamilybuylist.database.Purchase;

/**
 * Created by Дмитрий on 23.04.2017.
 */

public class DataManager extends RecyclerView.Adapter<DataManager.RecyclerViewHolder> {

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        CheckBox mBought;


        public RecyclerViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.item_buy_list_textview);
            mBought = (CheckBox) itemView.findViewById(R.id.item_buy_list_checkbox);

        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buy_list, parent, false);
        return new RecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        final Purchase purchase = Purchase.PURCHASE_LIST.get(position);
        holder.mName.setText(purchase.getName());
        holder.mBought.setChecked(purchase.isBought());
        if (purchase.isBought()) {
//            TODO: make checked items STRIKED THROUGH
/*

Instead of doing:

tv.setText(s);
do:

private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();
...
tv.setText(s, TextView.BufferType.SPANNABLE);
Spannable spannable = (Spannable) tv.getText();
spannable.setSpan(STRIKE_THROUGH_SPAN, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

 */
        }

    }

    @Override
    public int getItemCount() {
        return Purchase.PURCHASE_LIST.size();
    }


}
