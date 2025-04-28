package com.example.raifazenbank.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.raifazenbank.R;
import com.example.raifazenbank.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private List<String> notificationsList;
    private NotificationsAdapter adapter;
    private NotificationsViewModel notificationsViewModel;

    private List<String> previousNotifications = new ArrayList<>();
    private boolean firstLoad = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ListView listView = binding.listView;
        notificationsList = new ArrayList<>();
        adapter = new NotificationsAdapter(notificationsList);
        listView.setAdapter(adapter);

        View loadingLayout = binding.loadingLayout;
        loadingLayout.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        notificationsViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                if (firstLoad) {
                    firstLoad = false;
                    previousNotifications.clear();
                    previousNotifications.addAll(notifications);
                }

                notificationsList.clear();
                notificationsList.addAll(notifications);

                listView.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
            } else {
                listView.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class NotificationsAdapter extends BaseAdapter {
        private final List<String> items;

        public NotificationsAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_item, parent, false);
            }

            TextView recipientView = convertView.findViewById(R.id.recipient);
            TextView messageView = convertView.findViewById(R.id.message);
            TextView dateView = convertView.findViewById(R.id.date);
            TextView textMsg = convertView.findViewById(R.id.textMsg);

            String[] parts = items.get(position).split("\n");
            if (parts.length == 4) {
                recipientView.setText(parts[0]);
                messageView.setText(parts[1]);
                textMsg.setText(parts[2]);
                dateView.setText(parts[3]);
            }

            return convertView;
        }
    }
}