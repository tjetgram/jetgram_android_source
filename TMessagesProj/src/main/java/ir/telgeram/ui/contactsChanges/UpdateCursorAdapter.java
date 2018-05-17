package ir.telgeram.ui.contactsChanges;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ir.telgeram.ui.Components.BackupImageView;

public class UpdateCursorAdapter extends CursorAdapter {
    private a dataBaseAccess;

    public class ViewHolder {
        BackupImageView avatarImageView;
        TextView tvNewValue;
        TextView tvOldValue;
    }

    public UpdateCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        this.dataBaseAccess = new a();
    }

    public void bindView(View view, Context context, Cursor cursor) {
        ((UpdateCell) view).setData(dataBaseAccess.a(cursor));
    }

    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return new UpdateCell(this.mContext, 10);
    }
}
