package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.FormatUtils;
import com.udacity.stockhawk.data.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {

        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        String name = cursor.getString(Contract.Quote.POSITION_NAME);
        float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);
        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        holder.symbol.setText(symbol);
        holder.price.setText(FormatUtils.getDollarFormatUnsigned().format(price));

        String a11yChangeDirection;

        if (rawAbsoluteChange >= 0) {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            a11yChangeDirection = context.getString(R.string.a11y_positive_change);
        } else {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            a11yChangeDirection = context.getString(R.string.a11y_negative_change);
        }

        String change = FormatUtils.getDollarFormatSigned().format(rawAbsoluteChange);
        String percentage = FormatUtils.getPercentageFormatSigned().format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(context).equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
        } else {
            holder.change.setText(percentage);
        }

        // <!-- [name] current price [price] [up/down] [$/%] -->
        // Nicer contentDescriptions for items in the stock list
        String contentDescription = context.getString(R.string.a11y_list_item,
                name,
                holder.price.getText(),
                a11yChangeDirection,
                holder.change.getText());

        LinearLayout container = (LinearLayout) holder.symbol.getParent();
        container.setContentDescription(contentDescription);

        Timber.d(contentDescription);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn));

        }


    }
}
