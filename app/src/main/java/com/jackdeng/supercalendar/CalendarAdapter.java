package com.jackdeng.supercalendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日历gridview中的每一个item显示的textview
 * 
 * @author jack
 * @Email 1032938431@qq.com
 *
 */
public class CalendarAdapter extends BaseAdapter {
	private String[] mSpecialDay;
	private  boolean isShowOthersMonthDays;
	private  boolean isShowLunar;
	private boolean isLeapyear = false; // 是否为闰年
	private int daysOfMonth = 0; // 某月的天数
	private int dayOfWeek = 0; // 具体某一天是星期几
	private int lastDaysOfMonth = 0; // 上一个月的总天数
	private Context context;
	private String[] dayNumber = new String[42]; // 一个gridview中的日期存入此数组中
	// private static String week[] = {"周日","周一","周二","周三","周四","周五","周六"};
	private LunarCalendar lc = null;
	private Resources res = null;
	private Drawable drawable = null;

	private String currentYear = "";
	private String currentMonth = "";
	private String currentDay = "";

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
	private int currentFlag = -1; // 用于标记当天
	private int[] schDateTagFlag = null; // 存储当月所有的日程日期

	private String showYear = ""; // 用于在头部显示的年份
	private String showMonth = ""; // 用于在头部显示的月份
	private String animalsYear = "";
	private String leapMonth = ""; // 闰哪一个月
	private String cyclical = ""; // 天干地支
	// 系统当前时间
	private String sysDate = "";
	private String sys_year = "";
	private String sys_month = "";
	private String sys_day = "";
	private int tempNextMonthDay = 1;
	private boolean isShowToDay = true;

	public CalendarAdapter() {
		Date date = new Date();
		sysDate = sdf.format(date); // 当期日期
		sys_year = sysDate.split("-")[0];
		sys_month = sysDate.split("-")[1];
		sys_day = sysDate.split("-")[2];

	}

	public CalendarAdapter(Context context, Resources rs, boolean isShowOthersMonthDays,boolean isShowLunar,String[] specialDay,
						   int jumpMonth, int jumpYear, int year_c, int month_c, int day_c) {
		this();
		this.isShowOthersMonthDays = isShowOthersMonthDays;
		this.isShowLunar = isShowLunar;
		this.mSpecialDay = specialDay;
		this.context = context;
		lc = new LunarCalendar(mSpecialDay);
		this.res = rs;

		int stepYear = year_c + jumpYear;
		int stepMonth = month_c + jumpMonth;
		if (stepMonth > 0) {
			// 往下一个月滑动
			if (stepMonth % 12 == 0) {
				stepYear = year_c + stepMonth / 12 - 1;
				stepMonth = 12;
			} else {
				stepYear = year_c + stepMonth / 12;
				stepMonth = stepMonth % 12;
			}
		} else {
			// 往上一个月滑动
			stepYear = year_c - 1 + stepMonth / 12;
			stepMonth = stepMonth % 12 + 12;
			if (stepMonth % 12 == 0) {

			}
		}

		currentYear = String.valueOf(stepYear); // 得到当前的年份
		currentMonth = String.valueOf(stepMonth); // 得到本月
													// （jumpMonth为滑动的次数，每滑动一次就增加一月或减一月）
		currentDay = String.valueOf(day_c); // 得到当前日期是哪天

		getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));

	}

	public void setShowOthersMonthDays(boolean isShowOthersMonthDays){
		this.isShowOthersMonthDays =isShowOthersMonthDays;
		notifyDataSetChanged();
	}

	public void setShowToDay(boolean isShowToDay){
		this.isShowToDay =isShowToDay;
	}

	public void setSpecialDay(String[] specialDay){
		this.mSpecialDay =specialDay;
		lc = new LunarCalendar(mSpecialDay);
		getCalendar(Integer.parseInt(currentYear), Integer.parseInt(currentMonth));
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return isShowOthersMonthDays ? dayNumber.length :(dayNumber.length - tempNextMonthDay+1);//去除下月的item,不显示
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.calendar_item, null);
		}
		CheckedTextView textView = (CheckedTextView) convertView.findViewById(R.id.tvtext);
		String d = dayNumber[position].split("\\.")[0];
		String dv = dayNumber[position].split("\\.")[1];

		SpannableString sp = new SpannableString(d + "\n" + dv);
		sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		sp.setSpan(new RelativeSizeSpan(1.2f), 0, d.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (!TextUtils.isEmpty(dv)) {
			sp.setSpan(new RelativeSizeSpan(0.6f), d.length() + 1, dayNumber[position].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		textView.setText(sp);
		if (position < (daysOfMonth + dayOfWeek) && position >= dayOfWeek) {
			// 当前月信息显示
			textView.setTextColor(context.getResources().getColorStateList(R.color.selector_daytextcolor_black));// 当月字体设黑
//			if (position % 7 == 0 || position % 7 == 6) {//周末特殊标记
//			}
		}else {
			textView.setClickable(false);
			textView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			textView.setTextColor(context.getResources().getColor(R.color.black_80));//上一个月数据和下个月数据
			textView.setVisibility(isShowOthersMonthDays?View.VISIBLE:View.INVISIBLE);
		}

		if (isShowToDay && currentFlag == position) {
			// 设置当天的背景
			textView.setChecked(true);
		}

		return convertView;
	}

	// 得到某年的某月的天数且这月的第一天是星期几
	public void getCalendar(int year, int month) {
		isLeapyear = CalendarUtils.isLeapYear(year); // 是否为闰年
		daysOfMonth = CalendarUtils.getDaysOfMonth(isLeapyear, month); // 某月的总天数
		dayOfWeek = CalendarUtils.getWeekdayOfMonth(year, month); // 某月第一天为星期几
		lastDaysOfMonth = CalendarUtils.getDaysOfMonth(isLeapyear, month - 1); // 上一个月的总天数
		Log.d("DAY", isLeapyear + " ======  " + daysOfMonth + "  ============  " + dayOfWeek + "  =========   " + lastDaysOfMonth);
		getweek(year, month);
	}

	// 将一个月中的每一天的值添加入数组dayNuber中
	private void getweek(int year, int month) {
		int j = 1;
		tempNextMonthDay = 1;
		String lunarDay = "";

		// 得到当前月的所有日程日期(这些日期需要标记)

		for (int i = 0; i < dayNumber.length; i++) {
			// 周一
			// if(i<7){
			// dayNumber[i]=week[i]+"."+" ";
			// }
			if (i < dayOfWeek) { // 前一个月
				int temp = lastDaysOfMonth - dayOfWeek + 1;
				lunarDay = lc.getLunarDate(year, month - 1, temp + i, false,isShowLunar);
				dayNumber[i] = (temp + i) + "." + lunarDay;

			} else if (i < daysOfMonth + dayOfWeek) { // 本月
				String day = String.valueOf(i - dayOfWeek + 1); // 得到的日期
				lunarDay = lc.getLunarDate(year, month, i - dayOfWeek + 1, false,isShowLunar);
				dayNumber[i] = i - dayOfWeek + 1 + "." + lunarDay;
				// 对于当前月才去标记当前日期
				if (sys_year.equals(String.valueOf(year)) && sys_month.equals(String.valueOf(month)) && sys_day.equals(day)) {
					// 标记当前日期
					currentFlag = i;
				}
				setShowYear(String.valueOf(year));
				setShowMonth(String.valueOf(month));
//				setAnimalsYear(lc.animalsYear(year));
				setLeapMonth(lc.leapMonth == 0 ? "" : String.valueOf(lc.leapMonth));
//				setCyclical(lc.cyclical(year));
			} else { // 下一个月
				lunarDay = lc.getLunarDate(year, month + 1, j, false,isShowLunar);
				dayNumber[i] = j + "." + lunarDay;
				j++;
				tempNextMonthDay ++;
			}
		}

		String abc = "";
		for (int i = 0; i < dayNumber.length; i++) {
			abc = abc + dayNumber[i] + ":";
		}
		Log.d("DAYNUMBER", abc);

	}

	public void matchScheduleDate(int year, int month, int day) {

	}

	/**
	 * 点击每一个item时返回item中的日期
	 * 
	 * @param position
	 * @return
	 */
	public String getDateByClickItem(int position) {
		return dayNumber[position];
	}

	/**
	 * 在点击gridView时，得到这个月中第一天的位置
	 * 
	 * @return
	 */
	public int getStartPositon() {
		return dayOfWeek + 7;
	}

	/**
	 * 在点击gridView时，得到这个月中最后一天的位置
	 * 
	 * @return
	 */
	public int getEndPosition() {
		return (dayOfWeek + daysOfMonth + 7) - 1;
	}

	public String getShowYear() {
		return showYear;
	}

	public void setShowYear(String showYear) {
		this.showYear = showYear;
	}

	public String getShowMonth() {
		return showMonth;
	}

	public void setShowMonth(String showMonth) {
		this.showMonth = showMonth;
	}

	public String getAnimalsYear() {
		return animalsYear;
	}

	public void setAnimalsYear(String animalsYear) {
		this.animalsYear = animalsYear;
	}

	public String getLeapMonth() {
		return leapMonth;
	}

	public void setLeapMonth(String leapMonth) {
		this.leapMonth = leapMonth;
	}

	public String getCyclical() {
		return cyclical;
	}

	public void setCyclical(String cyclical) {
		this.cyclical = cyclical;
	}
}
