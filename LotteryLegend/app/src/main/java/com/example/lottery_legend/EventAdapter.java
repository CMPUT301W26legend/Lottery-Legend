package com.example.lottery_legend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventList;
    private String currentDeviceId;

    // https://developer.android.com/develop/ui/views/layout/recyclerview#java
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView status;
        TextView locationText;
        TextView deadline;
        TextView waitingCount;
        LinearLayout locationRow;
        Button joinButton;
        ImageView posterImage;
        LinearLayout cardContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            status = itemView.findViewById(R.id.status);
            locationText = itemView.findViewById(R.id.locationText);
            locationRow = itemView.findViewById(R.id.locationRow);
            deadline = itemView.findViewById(R.id.deadline);
            waitingCount = itemView.findViewById(R.id.waitingCount);
            joinButton = itemView.findViewById(R.id.joinButton);
            posterImage = itemView.findViewById(R.id.posterImage);
            cardContent = itemView.findViewById(R.id.cardContent);
        }
    }

    public EventAdapter(List<Event> eventList, String currentDeviceId) {
        this.eventList = eventList;
        this.currentDeviceId = currentDeviceId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Event event = eventList.get(position);

        holder.title.setText(event.getTitle());
        holder.locationText.setText(event.getLocation());
        holder.deadline.setText(event.getRegistrationEndDate());
        holder.waitingCount.setText(String.valueOf(event.getWaitingList().size()));

        holder.locationRow.setVisibility(event.isGeoEnabled() ? View.VISIBLE : View.GONE);

        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmap != null) {
                    holder.posterImage.setImageBitmap(bitmap);
                } else {
                    holder.posterImage.setImageResource(R.drawable.img_poster);
                }
            } catch (Exception e) {
                holder.posterImage.setImageResource(R.drawable.img_poster);
            }
        } else {
            holder.posterImage.setImageResource(R.drawable.img_poster);
        }

        boolean isJoined = event.getWaitingList().contains(currentDeviceId);

        setAlpha(holder, 1.0f);

        if (!Objects.equals(event.getStatus(), "open")) {
            holder.status.setText("Closed");
            holder.status.setTextColor(Color.parseColor("#64748B"));
            holder.joinButton.setVisibility(View.GONE);
            setAlpha(holder, 0.5f);
        } else if (isJoined) {
            holder.status.setText("Joined");
            holder.status.setTextColor(Color.parseColor("#F59E0B"));
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setText("Leave Waiting List");

            holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));

            holder.joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Leave Waiting List");
                }
            });
        } else {
            holder.status.setText("Open");
            holder.status.setTextColor(Color.parseColor("#10B981"));
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setText("Join WaitingList");
            holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));

            holder.joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Join Waiting List");
                }
            });
        }
    }

    private void setAlpha(ViewHolder holder, float alpha) {
        holder.cardContent.getChildAt(1).setAlpha(alpha);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
