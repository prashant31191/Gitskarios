package com.alorma.github.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alorma.github.R;
import com.alorma.github.sdk.bean.dto.response.GistFile;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.octicons_typeface_library.Octicons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bernat on 02/04/2015.
 */
public class GistDetailFilesAdapter extends RecyclerView.Adapter<GistDetailFilesAdapter.ViewHolder> {

    private final IconicsDrawable noPreviewDrawable;
    private List<GistFile> gistFileList;
    boolean isInEditMode = false;
    private GistFilesAdapterListener gistFilesAdapterListener;

    public GistDetailFilesAdapter(Context context) {
        gistFileList = new ArrayList<>();

        noPreviewDrawable = new IconicsDrawable(context, Octicons.Icon.oct_package);
        noPreviewDrawable.sizeDp(100);
        noPreviewDrawable.colorRes(R.color.secondary_text);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GistFile gistFile = gistFileList.get(position);

        if (holder.textFileName != null) {
            holder.textFileName.setText(gistFile.filename);
        }

        if (gistFile.type != null) {
            if (gistFile.type.contains("image")) {
                if (holder.imageContent != null) {
                    holder.imageContent.setImageDrawable(noPreviewDrawable);
                }
            } else {
                if (holder.textContent != null) {
                    printContent(holder.textContent, gistFile.content);
                }
            }
        }

        if (holder.toolbar != null) {
            if (isInEditMode) {
                holder.toolbar.setVisibility(View.GONE);
            } else {
                holder.toolbar.setVisibility(View.VISIBLE);
            }
        }
    }

    private void printContent(TextView textContent, String content) {
        try {
            content = splitLines(content);
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        textContent.setText(content);
    }

    private String splitLines(String content) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new StringReader(content));
        String line = reader.readLine();
        while (line != null) {
            result.add(line);
            line = reader.readLine();
        }
        if (result.size() > 10) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                if (result.get(i).isEmpty()) {
                    builder.append("\n");
                } else {
                    builder.append(result.get(i));
                    builder.append("\n");
                }
            }
            content = builder.toString();
        }
        return content;
    }

    @Override
    public int getItemCount() {
        return gistFileList.size();
    }

    @Override
    public int getItemViewType(int position) {
        GistFile gistFile = gistFileList.get(position);
        if (gistFile.type != null) {
            if (gistFile.type.contains("image")) {
                return R.layout.row_gist_detail_binari;
            } else {
                return R.layout.row_gist_detail_text;
            }
        } else {
            return R.layout.row_gist_detail_empty;
        }
    }

    public void add(GistFile item) {
        gistFileList.add(item);
        notifyItemInserted(gistFileList.size());
    }

    public void addAll(List<GistFile> items) {
        gistFileList.addAll(items);
        notifyItemInserted(gistFileList.size());
    }

    public List<GistFile> getGistFileList() {
        return this.gistFileList;
    }

    public void newEmptyItem() {
        GistFile gistFile = new GistFile();
        gistFileList.add(0, gistFile);
        notifyItemInserted(0);
    }

    public void updateCurrentItem(String title, String text) {
        if (getItemCount() > 0) {
            GistFile gistFile = gistFileList.get(0);
            gistFile.filename = title;
            gistFile.content = text;
            gistFile.type = "text/plain";
        }
    }

    public void updateItem(int position, GistFile updatedFile) {
        if (updatedFile != null && getItemCount() >= position) {
            GistFile file = gistFileList.get(position);
            file.filename = updatedFile.filename;
            file.content = updatedFile.content;
            notifyItemChanged(position);
        }
    }

    public void setInEditMode(boolean inEditMode) {
        this.isInEditMode = inEditMode;
        notifyDataSetChanged();
    }

    public void addFile(GistFile file) {
        if (getItemCount() > 0) {
            gistFileList.remove(0);
            notifyItemRemoved(0);
            gistFileList.add(0, file);
            notifyItemInserted(0);
        }
    }

    public void clearCurrent() {
        if (getItemCount() > 0) {
            gistFileList.remove(0);
            notifyItemRemoved(0);
        }
    }

    public void setGistFilesAdapterListener(GistFilesAdapterListener gistFilesAdapterListener) {
        this.gistFilesAdapterListener = gistFilesAdapterListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements Toolbar.OnMenuItemClickListener {
        public final TextView textFileName;
        public final TextView textContent;
        public final ImageView imageContent;
        private final Toolbar toolbar;

        public ViewHolder(View itemView) {
            super(itemView);
            textFileName = (TextView) itemView.findViewById(R.id.textFileName);
            textContent = (TextView) itemView.findViewById(R.id.textContent);
            imageContent = (ImageView) itemView.findViewById(R.id.imageContent);
            CardView cardView = (CardView) itemView.findViewById(R.id.cardView);

            toolbar = (Toolbar) itemView.findViewById(R.id.toolbar);

            if (toolbar != null) {
                toolbar.inflateMenu(R.menu.row_gist_file);
                toolbar.setOnMenuItemClickListener(this);
            }

            if (cardView != null) {
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (gistFilesAdapterListener != null) {
                            GistFile file = gistFileList.get(getAdapterPosition());
                            gistFilesAdapterListener.onGistFilesSelected(getAdapterPosition(), file);
                        }
                    }
                });
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_toolbar_share) {
                GistFile gistFile = gistFileList.get(getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, gistFile.filename);
                intent.putExtra(Intent.EXTRA_SUBJECT, gistFile.filename);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(gistFile.rawUrl));
                intent.putExtra(Intent.EXTRA_TEXT, gistFile.content);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
                textFileName.getContext().startActivity(Intent.createChooser(intent, textFileName.getResources().getString(R.string.send_file_to) + gistFile.filename));
            }
            return false;
        }
    }

    public interface GistFilesAdapterListener {
        void onGistFilesSelected(int position, GistFile file);
    }
}
