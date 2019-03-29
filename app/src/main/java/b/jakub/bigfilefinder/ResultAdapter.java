package b.jakub.bigfilefinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import b.jakub.bigfilefinder.R;
import java.io.File;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private List<File> files;
    private Context context;

    ResultAdapter(@NonNull Context context, List<File> files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.result_item, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tvFileName.setText(files.get(viewHolder.getAdapterPosition()).getName());
        viewHolder.tvFileSize.setText(getSizeOfFile(viewHolder.getAdapterPosition()));
        viewHolder.tvFilePath.setText(files.get(viewHolder.getAdapterPosition()).getParent());

    }

    private String getSizeOfFile(int adapterPosition) {
        File file = files.get(adapterPosition);
        long size;
            size = file.length() / 1024;
            if (size >= 1024) {
                return (size / 1024) + context.getString(R.string.mb);
            } else {
                return size + context.getString(R.string.kb);
            }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName, tvFilePath, tvFileSize;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileSize = itemView.findViewById(R.id.tv_file_size);
            tvFilePath = itemView.findViewById(R.id.tv_file_path);
        }
    }
}
