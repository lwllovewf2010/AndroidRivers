/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.android_rivers

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import com.silverkeytech.android_rivers.db.Bookmark
import com.silverkeytech.android_rivers.db.removeBookmarkFromCollection

public class BookmarkCollectionRenderer(val context: BookmarkCollectionActivity){
    class object {
        public val TAG: String = javaClass<BookmarkCollectionRenderer>().getSimpleName()
    }

    fun handleListing(bookmarks: List<Bookmark>) {

        if (bookmarks.count() == 0){
            var msg = context.findViewById(R.id.collection_message_tv)!! as TextView
            msg.setText(context.getString(R.string.empty_bookmark_collection_items_list))
        }

        val adapter = object : ArrayAdapter<Bookmark>(context, android.R.layout.simple_list_item_1, android.R.id.text1, bookmarks){
            public override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
                val text = bookmarks[position].toString()
                return currentListItem(text, convertView, parent)
            }
        }

        val list = context.findView<ListView>(android.R.id.list)
        list.setAdapter(adapter)
        list.setOnItemClickListener(object : OnItemClickListener{
            public override fun onItemClick(p0: AdapterView<out Adapter?>?, p1: View?, p2: Int, p3: Long) {
                val bookmark = bookmarks.get(p2)
                Log.d(TAG, "Downloading feed ${bookmark.title} - ${bookmark.url}")
                startFeedActivity(context, bookmark.url, bookmark.title, bookmark.language)
            }
        })

        list.setOnItemLongClickListener(object : AdapterView.OnItemLongClickListener{
            public override fun onItemLongClick(p0: AdapterView<out Adapter?>?, p1: View?, p2: Int, p3: Long): Boolean {
                val currentBookmark = bookmarks.get(p2)
                showCollectionQuickActionPopup(context, currentBookmark, p1!!, list)
                return true
            }
        })
    }

    public data class ViewHolder (var name: TextView)

    fun currentListItem(text: String, convertView: View?, parent: ViewGroup?): View? {
        var holder: ViewHolder?

        var vw: View? = convertView

        if (vw == null){
            vw = inflater().inflate(android.R.layout.simple_list_item_1, parent, false)

            holder = ViewHolder(vw!!.findViewById(android.R.id.text1) as TextView)
            holder!!.name.setText(text)
            vw!!.setTag(holder)
        }else{
            holder = vw!!.getTag() as ViewHolder
            holder!!.name.setText(text)
        }

        return vw
    }

    fun inflater(): LayoutInflater {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater
    }
}

fun showCollectionQuickActionPopup(context: BookmarkCollectionActivity, bookmark: Bookmark, item: View, list: View) {
    //overlay popup at top of clicked overview position
    val popupWidth = item.getWidth()
    val popupHeight = item.getHeight()

    val x = context.getLayoutInflater()!!.inflate(R.layout.collection_quick_actions, null, false)!!
    val pp = PopupWindow(x, popupWidth, popupHeight, true)

    x.setBackgroundColor(android.graphics.Color.LTGRAY)

    x.setOnClickListener {
        pp.dismiss()
    }

    val icon = x.findViewById(R.id.collection_quick_action_delete_icon) as ImageView
    icon.setOnClickListener {
        try{
            var res = removeBookmarkFromCollection(bookmark.collection!!.id, bookmark.id)

            if (res.isFalse())
                context.toastee("Error in removing this bookmark collection ${res.exception?.getMessage()}")
            else {
                context.toastee(context.getString(R.string.bookmark_removed)!!)
                context.refreshCollection()
            }
        }
        catch(e: Exception){
            context.toastee("Error in trying to remove this bookmark ${e.getMessage()}")
        }
        pp.dismiss()
    }

    val itemLocation = getLocationOnScreen(item)
    pp.showAtLocation(list, Gravity.TOP or Gravity.LEFT, itemLocation.x, itemLocation.y)
}
