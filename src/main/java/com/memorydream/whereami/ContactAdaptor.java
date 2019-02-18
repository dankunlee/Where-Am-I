package com.memorydream.whereami;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ContactAdaptor extends ArrayAdapter<Contact> {

    Context thisContext;

    public ContactAdaptor(Context context, int resource, List<Contact> contactList) {
        super(context, resource, contactList);
        thisContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        String contactName, contactPhoneNumb;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_contact, null);
        }

        final Contact contact = getItem(position);
        if (contact != null) {
            contactName = contact.getName();
            contactPhoneNumb = contact.getPhoneNumb();
            TextView editText_displayName = (TextView) convertView.findViewById(R.id.editText_displayName);
            editText_displayName.setText(contactName);
            TextView editText_displayPhoneNumb = (TextView) convertView.findViewById(R.id.editText_displayPhoneNumb);
            editText_displayPhoneNumb.setText(contactPhoneNumb);

        }
        return convertView;
    }
}
