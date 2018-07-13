package com.infomax.ibotncloudplayer;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.infomax.ibotncloudplayer.utils.MyLog;

public class GridViewUtils {

    private static String TAG = "GridViewUtils";

    /** 
     *  Save width 
     */  
    static SparseIntArray mGvWidth = new SparseIntArray();  
  
    /** 
     * Calculate gridview height.    
     */  
    public static void updateGridViewLayoutParams(MGridView gridView, int maxColumn) {
    	
    	if(gridView.getAdapter() == null){
    		return;
        }

        int childs = gridView.getAdapter().getCount();  
  
        if (childs > 0) {  
            int columns = childs < maxColumn ? childs % maxColumn : maxColumn;  
            gridView.setNumColumns(columns);  
            int width = 0;  
            int cacheWidth = mGvWidth.get(columns);  
            if (cacheWidth != 0) {  
                width = cacheWidth;  
            } else { 
                int rowCounts = childs < maxColumn ? childs : maxColumn;  //实际个数
                for (int i = 0; i < rowCounts; i++) {  
                    View childView = gridView.getAdapter().getView(i, null, gridView);  
                    childView.measure(0, 0);  
                    width += childView.getMeasuredWidth();
                    MyLog.d(TAG,"-->>>>:rowCounts:"+rowCounts+
                            "\n"+width+"\n"+childView.getMeasuredWidth());
                }  
            }  
  
            ViewGroup.LayoutParams params = gridView.getLayoutParams();  
            params.width = width;  
            gridView.setLayoutParams(params);  
            if (mGvWidth.get(columns) == 0) {  
                mGvWidth.append(columns, width);  
            }  
        }
    }

}
