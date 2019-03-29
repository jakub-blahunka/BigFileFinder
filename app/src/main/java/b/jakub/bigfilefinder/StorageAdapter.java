package b.jakub.bigfilefinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import b.jakub.bigfilefinder.R;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {
    private List<String> item;
    private List<String> path;
    private Context context;
    private Boolean isRoot;
    List<Integer> selectedItems;

    StorageAdapter(@NonNull Context context, List<String> item, List<String> path, Boolean isRoot) {
        this.context = context;
        this.item = item;
        this.path = path;
        this.isRoot = isRoot;
        selectedItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.storage_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final File isFile = new File((String) path.get(holder.getAdapterPosition()));
        if (getViewType(holder.getAdapterPosition()) == 1 && !isRoot) {
            holder.ivIcon.setImageResource(R.drawable.chevron_left);
            holder.tvSize.setVisibility(View.GONE);
        } else {
            holder.ivIcon.setImageResource(setFileImageType(new File(path.get(holder.getAdapterPosition()))));
        }
        holder.tvFileName.setText(item.get(holder.getAdapterPosition()));
        holder.tvSize.setText(getSizeOfItem(holder.getAdapterPosition()));
        if (!isFile.isDirectory() || (getViewType(holder.getAdapterPosition()) == 1 && !isRoot)) {
            holder.cbCheck.setVisibility(View.GONE);
        } else {
            holder.cbCheck.setVisibility(View.VISIBLE);
            holder.tvSize.setVisibility(View.GONE);
        }
        holder.cbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    selectedItems.add(holder.getAdapterPosition());
                } else {
                    selectedItems.remove(selectedItems.indexOf(holder.getAdapterPosition()));
                }
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFile.isDirectory()) {
                    ((Storage) context).setChildDirectoriesFromParentDirectory(isFile.toString());
                } else {
                    Toast.makeText(context, R.string.confirm_file, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    private int getViewType(int position) {
        if (position == 0) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int setFileImageType(File file) {
        if (file.isDirectory()) {
            return R.drawable.folder;
        } else {
            return R.drawable.attachment;
        }
    }

    private String getSizeOfItem(int position) {
        File file = new File(path.get(position));
        Long size = null;
        if (!file.isDirectory()) {
            size = file.length() / 1024;
            if (size >= 1024) {
                return (size / 1024) + context.getString(R.string.mb);
            } else {
                return size + context.getString(R.string.kb);
            }
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCheck;
        ImageView ivIcon;
        TextView tvFileName;
        TextView tvSize;
        LinearLayout layout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCheck = itemView.findViewById(R.id.cbCheck);
            ivIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvSize = itemView.findViewById(R.id.tvSize);
            layout = itemView.findViewById(R.id.LinearLayoutStorageItem);
        }
    }
}
