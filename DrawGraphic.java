package com.myapplication.draw;

import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.os.VibrationEffect;
import android.os.Build;
import android.os.Vibrator;
import java.io.Serializable;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams; // Импортируем LayoutParams для LinearLayout
import java.util.HashMap;

class HPoint implements Serializable { // Точка с цветом (выделенная)
	double time;
	double data;
	int color;
	
	public HPoint(double time0, double data0, int color0) {
		time = time0;
		data = data0;
		color = color0;
	}
	
	public double x() {
		return time;
	}
	
	public double y() {
		return data;
	}
}

class Line implements Serializable { // Линия с цветом и подписю
	HashMap<Integer, Double> time;
	HashMap<Integer, Double> data;
	int m2color;
	String legend;
	
	public Line(HashMap<Integer, Double> time0, HashMap<Integer, Double> data0, int m2color0, String legend0) {
		time = time0;
		data = data0;
		m2color = m2color0;
		legend = legend0;
	}
}

class info implements Serializable { // Информационный класс
	HashMap<Integer, Line> data;
	String name, name2; // Названия графика
	int width, height; // Ширина и высота рисунка
	int nx, ny; // Количество делений
	float window; // Какое окно изначально показывается
	float x1, x2, y1, y2; // Значения масштаба
	float lx1, lx2, ly1, ly2; // Изначальные значения
	int padd, marg; // Отступы
	int showleg; // Показывать ли легенды
	HashMap<Integer, HPoint> HPoints;
	
	public info(int width0, int height0, int nx0, int ny0, float window0, String name0, String name20,
	HashMap<Integer, Line> data0, float x10, float x20, float y10, float y20, float lx10, float lx20,
	float ly10, float ly20, int padd0, int marg0, int showleg0, HashMap<Integer, HPoint> HPoints0) {
		width = width0;
		height = height0;
		nx = nx0;
		ny = ny0;
		name = name0;
		name2 = name20;
		window = window0;
		data = data0;
		x1 = x10;
		x2 = x20;
		y1 = y10;
		y2 = y20;
		lx1 = lx10;
		lx2 = lx20;
		ly1 = ly10;
		ly2 = ly20;
		padd = padd0;
		marg = marg0;
		showleg = showleg0;
		HPoints = HPoints0;
	}
}

class Point { // Класс для хранения координат касания
	float x;
	float y;
	
	Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
}

public class DrawGraphic extends View { // Основной класс для рисования графиков
	private Paint fPaint = new Paint(); // Заливка
	private Paint mPaint = new Paint(); // Оси
	private Paint mPaint2 = new Paint(); //Вертикальные и горизонтальные оси
	private Paint m0Paint = new Paint(); // Пересекающиеся линии
	private Paint tPaint = new Paint(); // Текст
	private String name, name2 = "Секунды"; // Название графика
	private int width, height; // Ширина и высота рисунка
	private HashMap<Integer, Line> data; // Данные о линиях
	private float x1 = -10, x2 = 10, y1 = -5, y2 = 5; // Границы графика
	private float window = 10; // Размер стандартного окна по ОХ
	private int nx = 11, ny = 11; // Количество пересекающихся инф. линий
	private int counter = 0; // Счётчик изменения масштаба
	private float xx = 0, yy = 0; // Точка, около которой изменяем масштаб
	private HashMap<Integer, Point> touch1, touch2, touch3; // Для хранения координат касаний
	private int touchedPointIndex = -1; // Индекс выделенной точки
	private int twofs = 0; // Флаг, что было начато касание 2 пальцев
	private int flag; // Развернут график или свернут
	private int flag_v = 0, flag_h = 0, flag_d = 0; // Было ли начато горизонтальное/вертикальное/диагональное изменение
	private int padd = 50, marg = 30; // padding и margin отступы, можно переназначить, но не меньше заданных значений
	private int showleg = 0; // Показывать ли легенды
	private String dir = ""; // Для хранения информации о движении 2 пальцев
	private float hx = 0, hy = 0; // x и y выделенной точки
	private int fx = 0, fy = 0; // Координаты пальца
	private int num = 0; // Выбор линии для подсветки
	private long timer = 0; // Таймер для фиксации точки
	private int flag_fix = 0; // Была ли зафиксирована текущая точка
	HashMap<Integer, HPoint> HPoints; // Фиксированные точки
	
	// Необходимо для изменения режима - полноэкранный и обычный
	public int flagm = 0; // Необходимость изменения масштаба
	public info i; // Вся информация о графике
	
	public String errors = ""; // Для хранения информации об ошибках
	
	public int react = 1; // Нужно ли реагировать на касания
	
	// Конструктор класса
	public DrawGraphic(Context context, int width0, int height0, int nx0, int ny0, double window0, String name0,
	String name20, HashMap<Integer, Line> data0, int flag0) {
		super(context);
		// Инициализация данных
		width = width0;
		height = height0;
		nx = nx0;
		ny = ny0;
		name = name0;
		name2 = name20;
		window = (float) window0;
		data = data0;
		get_xy(data);
		HPoints = new HashMap<>();
		i = new info(width, height, nx, ny, window, name, name2, data, x2 - window, x2, y1, y2, x1, x2, y1, y2, padd,
		marg, showleg, HPoints);
		x1 = x2 - window; // До этого запомнили максимальный масштаб
		// и сузили до заданного окна
		touch1 = new HashMap<>();
		touch2 = new HashMap<>();
		touch3 = new HashMap<>();
		flag = flag0; // Полноэкранный ли режим
		initializePaints();
	}
	
	// Конструктор класса с допустимыми длиной и шириной
	public DrawGraphic(Context context, int width0, int height0, int nx0, int ny0, double window0, String name0,
	String name20, HashMap<Integer, Line> data0, int flag0, int w0, int h0) {
		super(context);
		// Инициализация данных
		width = width0;
		height = height0;
		nx = nx0;
		ny = ny0;
		name = name0;
		name2 = name20;
		window = (float) window0;
		data = data0;
		get_xy(data);
		HPoints = new HashMap<>();
		i = new info(width, height, nx, ny, window, name, name2, data, x2 - window, x2, y1, y2, x1, x2, y1, y2, padd,
		marg, showleg, HPoints);
		renew_xy(w0,h0); // До этого запомнили максимальный масштаб
		// и сузили до заданного окна
		
		touch1 = new HashMap<>();
		touch2 = new HashMap<>();
		touch3 = new HashMap<>();
		flag = flag0; // Полноэкранный ли режим
		initializePaints();
	}
	
	public void renew_xy(int w0, int h0) { // Обновление окна
		get_xy(data);
		HPoints = new HashMap<>();
		if (x2 - x1 < w0)
		x2 = x1 + w0;
		if (y2 - y1 < h0)
		y2 = y1 + h0;
		
		i.x1 = x2 - window;
		i.x2 = x2;
		i.y1 = y1;
		i.y2 = y2;
		i.lx1 = x1;
		i.lx2 = x2;
		i.ly1 = y1;
		i.ly2 = y2;
		x1 = x2 - window; // До этого запомнили максимальный масштаб
	}
	
	public void setPaddMarg(int padd0, int marg0) { // Переназначение отступов
		padd = padd0;
		i.padd = padd;
		marg = marg0;
		i.marg = marg;
	}
	
	public DrawGraphic(Context context, info i0, int flag0) { // Конструктор для класса info
		super(context);
		// Инициализация данных
		width = i0.width;
		height = i0.height;
		nx = i0.nx;
		ny = i0.ny;
		name = i0.name;
		name2 = i0.name2;
		window = i0.window;
		data = i0.data;
		x1 = i0.x1;
		x2 = i0.x2;
		y1 = i0.y1;
		y2 = i0.y2;
		showleg = i0.showleg;
		padd = i0.padd;
		marg = i0.marg;
		HPoints = i0.HPoints;
		i = new info(width, height, nx, ny, window, name, name2, data, x1, x2, y1, y2, i0.lx1, i0.lx2, i0.ly1, i0.ly2,
		padd, marg, showleg, HPoints);
		touch1 = new HashMap<>();
		touch2 = new HashMap<>();
		touch3 = new HashMap<>();
		flag = flag0;
		if (flag == 1) {
			width = Resources.getSystem().getDisplayMetrics().widthPixels;
			height = Resources.getSystem().getDisplayMetrics().heightPixels;
		}
		initializePaints();
	}
	
	private void initializePaints() { // Инициализация красок
		fPaint.setStyle(Paint.Style.FILL);
		fPaint.setColor(Color.WHITE);
		mPaint.setColor(Color.BLACK);
		mPaint.setStrokeWidth(2);
		mPaint2.setColor(Color.GRAY);
		mPaint2.setStrokeWidth(2);
		tPaint.setColor(Color.BLACK);
		tPaint.setTextSize(18.0f);
		tPaint.setStyle(Paint.Style.FILL);
		tPaint.setStrokeWidth(1.0f);
		tPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
	}
	
	private void get_xy(HashMap<Integer, Line> data) { // Определение пределов графика
		double xmin = Double.MAX_VALUE, xmax = -Double.MAX_VALUE, ymin = Double.MAX_VALUE, ymax = -Double.MAX_VALUE;
		for (int j = 0; j <= data.size(); j++) {
			if (data.containsKey(j)) {
				Line l = data.get(j);
				for (int k = 0; k <= l.time.size(); k++)
				if (l.time.containsKey(k)) {
					double t = l.time.get(k);
					double d = l.data.get(k);
					if (t > xmax)
					xmax = t;
					if (t < xmin)
					xmin = t;
					if (d > ymax)
					ymax = d;
					if (d < ymin)
					ymin = d;
				}
			}
		}
		x1 = (float) xmin;
		x2 = (float) xmax;
		y1 = (float) ymin;
		y2 = (float) ymax;
	}
	
	@Override
	protected void onDraw(Canvas canvas) { // Переписывание функции прорисовки
		super.onDraw(canvas);
		canvas.save();
		canvas.scale(1.0f, 1.0f); // Масштабирование
		// Заливка фона
		canvas.drawPaint(fPaint);
		// Рисуем оси и графики
		//Границы рисунка
		canvas.drawLine(0, 0, 0, height, mPaint2);
		canvas.drawLine(0, height, width, height, mPaint2);
		canvas.drawLine(width, height, width, 0, mPaint2);
		canvas.drawLine(width, 0, 0, 0, mPaint2);
		drawAxes(canvas); // Оси
		drawBtns(canvas); // Кнопки
		drawDataLines(canvas); // Данные
		drawCursor(canvas); // Курсор при изменении масштаба
		drawHighlightedPoint(canvas); // Выделенные точки
		if (showleg == 1)
		drawLegend(canvas); // Легенда
		if (react == 0) {
			Paint gPaint = new Paint();
			gPaint.setColor(Color.GRAY);
			gPaint.setStyle(Paint.Style.FILL);
			gPaint.setAlpha(10);
			canvas.drawRect(0, 0, width, height, gPaint);
		}
		canvas.restore(); // Восстановление состояния канваса
	}
	
	private int x_to_index(float x) { // Реальный х в х канваса
		return marg + padd + Math.round((x - x1) / (x2 - x1) * (width - padd * 2 - marg * 2));
	}
	
	private int y_to_index(float y) { // Реальный у в у канваса
		return marg + padd + Math.round((y2 - y) / (y2 - y1) * (height - padd * 2 - marg * 2));
	}
	
	private float index_to_x(int index) { // х канваса в х реальный
		return x1 + ((index - (marg + padd)) / (float) (width - padd * 2 - marg * 2)) * (x2 - x1);
	}
	
	private float index_to_y(int index) { // у канваса в у реальный
		return y2 - ((index - (marg + padd)) / (float) (height - padd * 2 - marg * 2)) * (y2 - y1);
	}
	
	private void drawBtns(Canvas canvas) { // Прорисовка кнопок графика
		if (react == 1) {
			Paint tPaint2 = new Paint();
			tPaint2.setColor(Color.BLACK);
			tPaint2.setTextSize(50.0f);
			tPaint2.setStyle(Paint.Style.FILL);
			tPaint2.setStrokeWidth(1.0f);
			tPaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
			
			if (flag == 0)
			canvas.drawText("⛶", width - 50, 50, tPaint2);
			else
			canvas.drawText("_", width - 40, 40, tPaint2);
			canvas.drawLine(width - 55, 5, width - 55, 55, mPaint2);
			canvas.drawLine(width - 55, 55, width - 5, 55, mPaint2);
			canvas.drawLine(width - 5, 55, width - 5, 5, mPaint2);
			canvas.drawLine(width - 5, 5, width - 55, 5, mPaint2);
			
			if (x1 != i.lx2 - window || x2 != i.lx2 || y1 != i.ly1 || y2 != i.ly2) {
				canvas.drawText("Сброс", 20, 25, tPaint);
				canvas.drawText("масштаба", 10, 45, tPaint);
				canvas.drawLine(100, 5, 100, 50, mPaint2);
				canvas.drawLine(100, 50, 5, 50, mPaint2);
				canvas.drawLine(5, 50, 5, 5, mPaint2);
				canvas.drawLine(5, 5, 100, 5, mPaint2);
			}
			if (HPoints.size() > 0) {
				canvas.drawText("Сброс", width - 260, padd + marg - 35, tPaint);
				canvas.drawText("фиксаций", width - 275, padd + marg - 15, tPaint);
				canvas.drawLine(width - 285, padd + marg - 55, width - 285, padd + marg - 5, mPaint2);
				canvas.drawLine(width - 285, padd + marg - 5, width - 185, padd + marg - 5, mPaint2);
				canvas.drawLine(width - 185, padd + marg - 5, width - 185, padd + marg - 55, mPaint2);
				canvas.drawLine(width - 185, padd + marg - 55, width - 285, padd + marg - 55, mPaint2);
			}
			
			String s1, s2 = "подписи";
			int dx;
			if (showleg == 1) {
				s1 = "Скрыть";
				dx = 95;
				} else {
				s1 = "Показать";
				dx = 80;
			}
			
			canvas.drawText(s1, width - 10 * (s1).length() - dx, padd + marg - 35, tPaint);
			canvas.drawText(s2, width - 10 * (s2).length() - 85, padd + marg - 15, tPaint);
			canvas.drawLine(width - 170, padd + marg - 55, width - 70, padd + marg - 55, mPaint2);
			canvas.drawLine(width - 70, padd + marg - 55, width - 70, padd + marg - 5, mPaint2);
			canvas.drawLine(width - 70, padd + marg - 5, width - 170, padd + marg - 5, mPaint2);
			canvas.drawLine(width - 170, padd + marg - 5, width - 170, padd + marg - 55, mPaint2);
		}
	}
	
	private void drawAxes(Canvas canvas) { // Рисуем оси и подписи графика
		// Расстояния до осей
		int oh = y_to_index(0);
		int ow = x_to_index(0);
		//Горизонтальные и вертикальные линии
		drawXAxis(canvas, oh);
		drawYAxis(canvas, ow);
		//Ось ОХ
		if (y1 <= 0 && y2 >= 0) {
			canvas.drawLine(padd, oh, width - padd, oh, mPaint);
			if (x2 >= i.lx2) {
				canvas.drawLine(width - padd, oh, width - padd - 20, oh - 7, mPaint);
				canvas.drawLine(width - padd, oh, width - padd - 20, oh + 7, mPaint);
				canvas.drawText(name2, width - (name2.length() * 10 + 10), oh + -10, tPaint);
			}
		}
		//Ось ОY
		if (x1 <= 0 && x2 >= 0) {
			canvas.drawLine(ow, padd, ow, height - padd, mPaint);
			if (y2 >= i.ly2) {
				canvas.drawLine(ow, padd, ow - 7, padd + 20, mPaint);
				canvas.drawLine(ow, padd, ow + 7, padd + 20, mPaint);
				canvas.drawText(name, ow + 30, padd, tPaint);
			}
		}
		if (y1 <= 0 && y2 >= 0 && x1 <= 0 && x2 >= 0)
		canvas.drawText("0", ow - 15, oh + 15, tPaint);
	}
	
	private void drawXAxis(Canvas canvas, int oh) { //Вертикальные линии и их подписи
		float dx0 = (x2 - x1) / (nx - 1);
		//canvas.drawText(String.valueOf(x2 - x1),100,100,tPaint);
		int i = 0;
		if (Math.abs(dx0 - Math.round(dx0)) > 0)
		while (Math.abs(dx0 - Math.round(dx0)) <= Math.pow(10, -i))
		i++;
		float dx = dx0;
		// Прорисовка
		for (float x = x2; x >= x1 - dx / 2; x -= dx) {
			int x0 = x_to_index(x);
			canvas.drawLine(x0, padd + marg, x0, height - padd - marg, mPaint2);
			if (x < -Math.pow(10, -i - 1) || x > Math.pow(10, -i - 1)) {
				if (y1 <= 0 && y2 >= 0) {
					if (x0 <= width / 2) // Половина подписей слева
					canvas.drawText(String.format("%." + String.valueOf(i) + "f", x), x0 + 5, oh - 5, tPaint);
					else // Половина подписей справа
					canvas.drawText(String.format("%." + String.valueOf(i) + "f", x),
					x0 - (String.format("%." + String.valueOf(i) + "f", x)).length() * 10 - 5, oh + 15,
					tPaint);
				} else
				canvas.drawText(String.format("%." + String.valueOf(i) + "f", x),
				x0 - (String.format("%." + String.valueOf(i) + "f", x)).length() * 5,
				height - marg - padd + 15, tPaint);
			}
		}
	}
	
	private void drawYAxis(Canvas canvas, int ow) { //Горизонтальные линии и их подписи
		float dy0 = (y2 - y1) / (ny - 1);
		int i = 0;
		if (Math.abs(dy0 - Math.round(dy0)) > 0)
		while (Math.abs(dy0 - Math.round(dy0)) <= Math.pow(10, -i))
		i++;
		float dy = dy0;
		// Прорисовка
		for (float y = y2; y >= y1 - dy / 2; y -= dy) {
			int y0 = y_to_index(y);
			canvas.drawLine(padd + marg, y0, width - padd - marg, y0, mPaint2);
			if (y < -Math.pow(10, -i - 1) || y > Math.pow(10, -i - 1)) {
				if (x1 <= 0 && x2 >= 0)
				canvas.drawText(String.format("%." + String.valueOf(i) + "f", y), ow + 10, y0 - 2, tPaint);
				else
				canvas.drawText(String.format("%." + String.valueOf(i) + "f", y),
				marg + padd - 5 - 10 * String.format("%." + String.valueOf(i) + "f", y).length(), y0,
				tPaint);
			}
		}
	}
	
	private void drawDataLines(Canvas canvas) { // Линии графика
		for (int j = 1; j <= data.size(); j++) {
			Line l = data.get(j);
			Paint myPaint = new Paint();
			myPaint.setColor(l.m2color);
			myPaint.setStrokeWidth(2);
			drawSingleLine(canvas, l.time, l.data, myPaint);
		}
	}
	
	private void drawLegend(Canvas canvas) { // Рисование legend
		// Прорисовка прямоугольника
		int h = 0, w = 0;
		for (int j = 1; j <= i.data.size(); j++) {
			String s = i.data.get(j).legend;
			h = h + 20;
			if (s.length() * 10 + 20 > w)
			w = s.length() * 10 + 20;
		}
		int h0 = padd + marg + 5;
		canvas.drawRect(width - 60 - w, h0, width - 10, h0 + h, fPaint); // Прорисовка прямоугольника
		canvas.drawLine(width - 60 - w, h0, width - 10, h0, mPaint);
		canvas.drawLine(width - 10, h0, width - 10, h0 + h, mPaint);
		canvas.drawLine(width - 10, h0 + h, width - 60 - w, h0 + h, mPaint);
		canvas.drawLine(width - 60 - w, h0 + h, width - 60 - w, h0, mPaint);
		// Прорисовка подписей
		for (int j = 1; j <= i.data.size(); j++) {
			String s = i.data.get(j).legend;
			int c = i.data.get(j).m2color;
			Paint myPaint = new Paint();
			myPaint.setColor(c);
			myPaint.setStrokeWidth(2);
			int l1 = s.length() * 10 + 20;
			canvas.drawText(s, width - 60 - l1 + 50, h0 + 5 + 20 * j - 10, tPaint);
			canvas.drawLine(width - 60 - l1 + 5, h0 + 5 + 20 * j - 15, width - 60 - l1 + 45, h0 + 5 + 20 * j - 15,
			myPaint);
		}
	}
	
	private void drawSingleLine(Canvas canvas, HashMap<Integer, Double> time, HashMap<Integer, Double> data,
	Paint paint) { // Рисование одной из линий
		if (data != null) {
			int x01, y01, x0 = -1, y0 = -1;
			double i1, j1, i0 = 0, j0 = 0;
			for (int k0 = 0; k0 <= time.size(); k0++) {
				if (data.containsKey(k0) && k0 > 0) {
					i0 = time.get(k0 - 1);
					j0 = data.get(k0 - 1);
					i1 = time.get(k0);
					j1 = data.get(k0);
					// Уравнение прямой по 2 точкам
					// (x-i1)/(i2-i1)=(y-j1)/(j2-j1)
					// Есть прямые y=y1,y=y2,x=x1,x=x2
					double x_y1 = i0 + (y1 - j0) / (j1 - j0) * (i1 - i0);
					double x_y2 = i0 + (y2 - j0) / (j1 - j0) * (i1 - i0);
					double y_x1 = j0 + (x1 - i0) / (i1 - i0) * (j1 - j0);
					double y_x2 = j0 + (x2 - i0) / (i1 - i0) * (j1 - j0);
					x0 = 0;
					y0 = 0;
					x01 = 0;
					y01 = 0;
					// Пересечение нижней границы
					if (x_y1 >= i0 && x_y1 <= i1 && (y1 >= j0 && y1 <= j1 || y1 >= j1 && y1 <= j0)) {
						if (j0 < y1) {
							i0 = x_y1;
							j0 = y1;
							y0 = height - padd - marg;
						}
						if (j1 < y1) {
							i1 = x_y1;
							j1 = y1;
							y01 = height - padd - marg;
						}
					}
					// Пересечение верхней границы
					else if (x_y2 >= i0 && x_y2 <= i1 && (y2 >= j0 && y2 <= j1 || y2 >= j1 && y2 <= j0)) {
						if (j0 > y2) {
							i0 = x_y2;
							j0 = y2;
							y0 = padd + marg;
						}
						if (j1 > y2) {
							i1 = x_y2;
							j1 = y2;
							y01 = padd + marg;
						}
					}
					// Пересечение левой границы
					else if (x1 >= i0 && x1 <= i1 & (y_x1 >= j0 && y_x1 <= j1 || y_x1 >= j1 && y_x1 <= j0)) {
						if (i0 < x1) {
							i0 = x1;
							j0 = y_x1;
							x0 = padd + marg;
						}
					}
					// Пересечение правой границы
					else if (x2 >= i0 && x2 <= i1 && (y_x2 >= j0 && y_x2 <= j1 || y_x2 >= j1 && y_x2 <= j0)) {
						if (i1 > x2) {
							i1 = x2;
							j1 = y_x2;
							x01 = width - padd - marg;
						}
					}
					if (x0 == 0)
					x0 = x_to_index((float) i0);
					if (y0 == 0)
					y0 = y_to_index((float) j0);
					if (x01 == 0)
					x01 = x_to_index((float) i1);
					if (y01 == 0)
					y01 = y_to_index((float) j1);
					if (i0 >= x1 && i0 <= x2 && j0 >= y1 && j0 <= y2 && i1 >= x1 && i1 <= x2 && j1 >= y1 && j1 <= y2)
					canvas.drawLine(x0, y0, x01, y01, paint);
				}
			}
		}
	}
	
	private void findPoint(int x, int y) { // Поиск точки на графике, ближайшей к координатам касания
		int min = 100;
		if (num == 0)
		for (int j = 1; j <= i.data.size(); j++) {
			HashMap<Integer, Double> time = i.data.get(j).time;
			HashMap<Integer, Double> data = i.data.get(j).data;
			if (data != null) {
				for (int k0 = 0; k0 <= time.size(); k0++) {
					if (time.containsKey(k0)) {
						double i0 = time.get(k0);
						double j0 = data.get(k0);
						int x0 = x_to_index((float) i0);
						int y0 = y_to_index((float) j0);
						if (Math.abs(x0 - x) < 20 && Math.abs(y0 - y) < 20 && i0 >= x1 && i0 <= x2 && j0 >= y1
						&& j0 <= y2) {
							if (Math.round(Math.sqrt(Math.pow(x0 - x, 2) + Math.pow(y0 - y, 2))) < min) {
								hx = (float) i0;
								hy = (float) j0;
								num = j;
								min = (int) Math.round(Math.sqrt(Math.pow(x0 - x, 2) + Math.pow(y0 - y, 2)));
								flag_fix = 0;
								timer = 0;
							}
						}
					}
				}
			}
		}
		else {
			HashMap<Integer, Double> time = i.data.get(num).time;
			HashMap<Integer, Double> data = i.data.get(num).data;
			if (data != null) {
				for (int k0 = 0; k0 <= time.size(); k0++) {
					if (time.containsKey(k0)) {
						double i0 = time.get(k0);
						double j0 = data.get(k0);
						int x0 = x_to_index((float) i0);
						int y0 = y_to_index((float) j0);
						if (fy > height - padd - marg)
						if (Math.abs(x0 - x) < min && i0 >= x1 && i0 <= x2 && j0 >= y1 && j0 <= y2) {
							hx = (float) i0;
							hy = (float) j0;
							min = Math.abs(x0 - x);
							flag_fix = 0;
							timer = 0;
						}
					}
				}
			}
		}
	}
	
	private void drawHighlightedPoint(Canvas canvas) { // Ввделение фиксированных точек и обработка выделенной
		// Прорисовка и подписи фиксированных точек
		for (Integer key : HPoints.keySet()) {
			HPoint hp = HPoints.get(key);
			float x_ = (float) hp.x();
			float y_ = (float) hp.y();
			if (x_ >= x1 && x_ <= x2 && y_ >= y1 && y_ <= y2) {
				int c = hp.color;
				int x = x_to_index(x_);
				int y = y_to_index(y_);
				Paint myPaint = new Paint();
				myPaint.setColor(c);
				canvas.drawCircle(x, y, 5, myPaint);
				if (x + 5 < width / 2)
				canvas.drawText(String.format("x: %.2f, y: %.2f", x_, y_), x + 15, y, tPaint);
				else
				canvas.drawText(String.format("x: %.2f, y: %.2f", x_, y_),
				x - 10 - 7 * String.format("x: %.2f, y: %.2f", x_, y_).length(), y, tPaint);
			}
		}
		// Обработка выделенной точки
		if (num > 0) {
			int x = x_to_index(hx);
			int y = y_to_index(hy);
			int is_fixed = 0; // Была ли точка уже зафиксирована
			int index = 0; // Индекс уже зафиксированной точкм
			for (Integer key : HPoints.keySet()) {
				HPoint hp = HPoints.get(key);
				float x_ = (float) hp.x();
				float y_ = (float) hp.y();
				if (hx == x_ && hy == y_) {
					is_fixed = 1;
					index = key; // Здесь index будет ключом
					break; // Выход из цикла, если нашли элемент
				}
			}
			
			int c = i.data.get(num).m2color;
			if (is_fixed == 1)
			c = Color.BLACK;
			if (flag_fix == 0) {
				Paint myPaint = new Paint();
				myPaint.setColor(c);
				myPaint.setPathEffect(new DashPathEffect(new float[] { 30, 10 }, 0)); // Делаем линию пунктирной
				canvas.drawCircle(x, y, 10, myPaint);
				if (Math.sqrt((x - fx) * (x - fx) + (y - fy) * (y - fy)) >= 70)
				if (x + 5 < width / 2)
				canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy), x + 15, y, tPaint);
				else
				canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy),
				x - 10 - 7 * String.format("x: %.2f, y: %.2f", hx, hy).length(), y, tPaint);
				else {
					if (y > height / 2) {
						canvas.drawLine(x, y, x, y - 150, myPaint);
						if (x + 5 < width / 2)
						canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy), x + 5, y - 150, tPaint);
						else
						canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy),
						x - 7 * String.format("x: %.2f, y: %.2f", hx, hy).length(), y - 150, tPaint);
						} else {
						canvas.drawLine(x, y, x, y + 150, myPaint);
						if (x + 5 < width / 2)
						canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy), x + 5, y + 150, tPaint);
						else
						canvas.drawText(String.format("x: %.2f, y: %.2f", hx, hy),
						x - 7 * String.format("x: %.2f, y: %.2f", hx, hy).length(), y + 150, tPaint);
						
					}
				}
			}
			
			if (flag_fix == 2)
			canvas.drawText("Точка удалена", padd + marg * 8, padd, tPaint);
			else if (flag_fix == 1)
			canvas.drawText("Точка зафиксирована", padd + marg * 8, padd, tPaint);
			else if (fy >= padd + marg) {
				timer = 0;
				if (is_fixed == 0) {
					canvas.drawText("Для фиксации точки наведите пальцем", padd + marg * 8, padd, tPaint);
					canvas.drawText("вверх и подождите секунду", padd + marg * 8, padd + 20, tPaint);
					} else {
					canvas.drawText("Для удаления точки наведите пальцем", padd + marg * 8, padd, tPaint);
					canvas.drawText("вверх и подождите секунду", padd + marg * 8, padd + 20, tPaint);
				}
				} else {
				Paint tPaint2 = new Paint();
				tPaint2.setColor(Color.BLACK);
				tPaint2.setTextSize(50.0f);
				tPaint2.setStyle(Paint.Style.FILL);
				tPaint2.setStrokeWidth(1.0f);
				tPaint2.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
				if (timer == 0) {
					timer = System.currentTimeMillis();
					flag_fix = 0;
				}
				long time = System.currentTimeMillis() - timer;
				if (time <= 1000) {
					if (time > 0) {
						if (fx < width / 2)
						canvas.drawText(String.format("%.1f", time / 1000.0), fx + 150, padd, tPaint2);
						else
						canvas.drawText(String.format("%.1f", time / 1000.0), fx - 150, padd, tPaint2);
					}
					} else {
					//
					//
					//
					//
					//
					//
					// Логика фиксирования и удаления
					if (is_fixed == 0) {
						flag_fix = 1;
						// Вибрация об успехе фиксации
						Context context = getContext();
						Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
							} else {
							//для API 26
							v.vibrate(100);
						}
						HPoint hp = new HPoint(hx, hy, i.data.get(num).m2color);
						index = 1;
						while (HPoints.containsKey(index))
						index++;
						HPoints.put(index, hp);
						i.HPoints = HPoints;
						} else {
						flag_fix = 2;
						// Вибрация об успехе удаления
						Context context = getContext();
						Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
							} else {
							//для API 26
							v.vibrate(100);
						}
						// Удаляем точку с номером index
						HPoints.remove(index);
						i.HPoints = HPoints;
						
					}
				}
				
			}
			// Логика выбора другой точкм
			if (fy <= height - padd - marg)
			canvas.drawText("Для выбора другой точки наведите пальцем вниз", padd + marg * 3, height - padd + 10,
			tPaint);
			else {
				int p1 = fx - 150, p2 = fx + 150;
				if (p2 > padd + marg && p1 < width - padd - marg) {
					if (p1 < padd + marg)
					p1 = padd + marg;
					if (p2 > width - padd - marg)
					p2 = width - padd - marg;
					canvas.drawLine(p1, height - padd + 10, p2, height - padd + 10, mPaint);
					if (p1 > padd + marg) {
						canvas.drawLine(p1, height - padd + 10, p1 + 10, height - padd + 10 - 5, mPaint);
						canvas.drawLine(p1, height - padd + 10, p1 + 10, height - padd + 10 + 5, mPaint);
					}
					if (p2 < width - padd - marg) {
						canvas.drawLine(p2, height - padd + 10, p2 - 10, height - padd + 10 - 5, mPaint);
						canvas.drawLine(p2, height - padd + 10, p2 - 10, height - padd + 10 + 5, mPaint);
					}
				}
			}
		}
	}
	
	private void drawCursor(Canvas canvas) { //Курсор увеличения-уменьшения
		if (xx != 0 && yy != 0) {
			int x = x_to_index(xx), y = y_to_index(yy);
			canvas.drawLine(x, y - 15, x, y + 15, mPaint);
			canvas.drawLine(x - 15, y, x + 15, y, mPaint);
			if (dir == "Горизонтальное растяжение") {
				canvas.drawLine(x - 40, y, x - 20, y, mPaint);
				canvas.drawLine(x - 40, y, x - 30, y + 5, mPaint);
				canvas.drawLine(x - 40, y, x - 30, y - 5, mPaint);
				canvas.drawLine(x + 40, y, x + 20, y, mPaint);
				canvas.drawLine(x + 40, y, x + 30, y + 5, mPaint);
				canvas.drawLine(x + 40, y, x + 30, y - 5, mPaint);
			}
			if (dir == "Горизонтальное сжатие") {
				canvas.drawLine(x - 40, y, x - 20, y, mPaint);
				canvas.drawLine(x - 20, y, x - 30, y + 5, mPaint);
				canvas.drawLine(x - 20, y, x - 30, y - 5, mPaint);
				canvas.drawLine(x + 40, y, x + 20, y, mPaint);
				canvas.drawLine(x + 20, y, x + 30, y + 5, mPaint);
				canvas.drawLine(x + 20, y, x + 30, y - 5, mPaint);
			}
			if (dir == "Вертикальное растяжение") {
				canvas.drawLine(x, y - 40, x, y - 20, mPaint);
				canvas.drawLine(x, y - 40, x - 5, y - 30, mPaint);
				canvas.drawLine(x, y - 40, x + 5, y - 30, mPaint);
				canvas.drawLine(x, y + 40, x, y + 20, mPaint);
				canvas.drawLine(x, y + 40, x - 5, y + 30, mPaint);
				canvas.drawLine(x, y + 40, x + 5, y + 30, mPaint);
			}
			if (dir == "Вертикальное сжатие") {
				canvas.drawLine(x, y - 40, x, y - 20, mPaint);
				canvas.drawLine(x, y - 20, x - 5, y - 30, mPaint);
				canvas.drawLine(x, y - 20, x + 5, y - 30, mPaint);
				canvas.drawLine(x, y + 40, x, y + 20, mPaint);
				canvas.drawLine(x, y + 20, x - 5, y + 30, mPaint);
				canvas.drawLine(x, y + 20, x + 5, y + 30, mPaint);
			}
			// Для диагонального используются формулы для прямоугольного треугольника
			// и формулы для поворота отрезка вокруг оси на угол 30° (катеты 5 и 10)
			if (dir == "Диагональное сжатие") {
				// Вычисление вспомогательных значений
				double angle = Math.toRadians(30); // Преобразование угла поворота в радианы
				double cosTheta = Math.cos(angle);
				double sinTheta = Math.sin(angle);
				
				int dx = -(int) Math.round(10 / Math.sqrt(2)), dy = dx;
				// Вычисления значений для линий (первая группа)
				int x1_1 = (int) Math.round(x - 15 / 2), x1_2 = (int) Math.round(x - 15 / 2 - 20 / Math.sqrt(2));
				int y1_1 = (int) Math.round(y - 15 / 2), y1_2 = (int) Math.round(y - 15 / 2 - 20 / Math.sqrt(2));
				int x1_3 = (int) Math.round(x1_1 + (dx * cosTheta - dy * sinTheta));
				int y1_3 = (int) Math.round(y1_1 + (dx * sinTheta + dy * cosTheta));
				int x1_4 = (int) Math.round(x1_1 + (dx * cosTheta + dy * sinTheta));
				int y1_4 = (int) Math.round(y1_1 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x1_1, y1_1, x1_2, y1_2, mPaint);
				canvas.drawLine(x1_1, y1_1, x1_3, y1_3, mPaint);
				canvas.drawLine(x1_1, y1_1, x1_4, y1_4, mPaint);
				
				dx = -(int) Math.round(10 / Math.sqrt(2));
				dy = -dx;
				// Вычисления значений для линий (вторая группа, x2, y2)
				int x2_1 = (int) Math.round(x - 15 / 2), x2_2 = (int) Math.round(x - 15 / 2 - 20 / Math.sqrt(2));
				int y2_1 = (int) Math.round(y + 15 / 2), y2_2 = (int) Math.round(y + 15 / 2 + 20 / Math.sqrt(2));
				int x2_3 = (int) Math.round(x2_1 + (dx * cosTheta - dy * sinTheta));
				int y2_3 = (int) Math.round(y2_1 + (dx * sinTheta + dy * cosTheta));
				int x2_4 = (int) Math.round(x2_1 + (dx * cosTheta + dy * sinTheta));
				int y2_4 = (int) Math.round(y2_1 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x2_1, y2_1, x2_2, y2_2, mPaint);
				canvas.drawLine(x2_1, y2_1, x2_3, y2_3, mPaint);
				canvas.drawLine(x2_1, y2_1, x2_4, y2_4, mPaint);
				
				dx = (int) Math.round(10 / Math.sqrt(2));
				dy = -dx;
				// Вычисления значений для линий (третья группа, x3, y3)
				int x3_1 = (int) Math.round(x + 15 / 2), x3_2 = (int) Math.round(x + 15 / 2 + 20 / Math.sqrt(2));
				int y3_1 = (int) Math.round(y - 15 / 2), y3_2 = (int) Math.round(y - 15 / 2 - 20 / Math.sqrt(2));
				int x3_3 = (int) Math.round(x3_1 + (dx * cosTheta - dy * sinTheta));
				int y3_3 = (int) Math.round(y3_1 + (dx * sinTheta + dy * cosTheta));
				int x3_4 = (int) Math.round(x3_1 + (dx * cosTheta + dy * sinTheta));
				int y3_4 = (int) Math.round(y3_1 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x3_1, y3_1, x3_2, y3_2, mPaint);
				canvas.drawLine(x3_1, y3_1, x3_3, y3_3, mPaint);
				canvas.drawLine(x3_1, y3_1, x3_4, y3_4, mPaint);
				
				dx = (int) Math.round(10 / Math.sqrt(2));
				dy = dx;
				// Вычисления значений для линий (четвертая группа, x4, y4)
				int x4_1 = (int) Math.round(x + 15 / 2), x4_2 = (int) Math.round(x + 15 / 2 + 20 / Math.sqrt(2));
				int y4_1 = (int) Math.round(y + 15 / 2), y4_2 = (int) Math.round(y + 15 / 2 + 20 / Math.sqrt(2));
				int x4_3 = (int) Math.round(x4_1 + (dx * cosTheta - dy * sinTheta));
				int y4_3 = (int) Math.round(y4_1 + (dx * sinTheta + dy * cosTheta));
				int x4_4 = (int) Math.round(x4_1 + (dx * cosTheta + dy * sinTheta));
				int y4_4 = (int) Math.round(y4_1 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x4_1, y4_1, x4_2, y4_2, mPaint);
				canvas.drawLine(x4_1, y4_1, x4_3, y4_3, mPaint);
				canvas.drawLine(x4_1, y4_1, x4_4, y4_4, mPaint);
			}
			if (dir == "Диагональное растяжение") {
				// Вычисление вспомогательных значений
				double angle = Math.toRadians(30); // Преобразование угла поворота в радианы
				double cosTheta = Math.cos(angle);
				double sinTheta = Math.sin(angle);
				
				int dx = (int) Math.round(10 / Math.sqrt(2)), dy = dx;
				// Вычисления значений для линий (первая группа)
				int x1_1 = (int) Math.round(x - 15 / 2), x1_2 = (int) Math.round(x - 15 / 2 - 20 / Math.sqrt(2));
				int y1_1 = (int) Math.round(y - 15 / 2), y1_2 = (int) Math.round(y - 15 / 2 - 20 / Math.sqrt(2));
				int x1_3 = (int) Math.round(x1_2 + (dx * cosTheta - dy * sinTheta));
				int y1_3 = (int) Math.round(y1_2 + (dx * sinTheta + dy * cosTheta));
				int x1_4 = (int) Math.round(x1_2 + (dx * cosTheta + dy * sinTheta));
				int y1_4 = (int) Math.round(y1_2 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x1_1, y1_1, x1_2, y1_2, mPaint);
				canvas.drawLine(x1_2, y1_2, x1_3, y1_3, mPaint);
				canvas.drawLine(x1_2, y1_2, x1_4, y1_4, mPaint);
				
				dx = (int) Math.round(10 / Math.sqrt(2));
				dy = -dx;
				// Вычисления значений для линий (вторая группа, x2, y2)
				int x2_1 = (int) Math.round(x - 15 / 2), x2_2 = (int) Math.round(x - 15 / 2 - 20 / Math.sqrt(2));
				int y2_1 = (int) Math.round(y + 15 / 2), y2_2 = (int) Math.round(y + 15 / 2 + 20 / Math.sqrt(2));
				int x2_3 = (int) Math.round(x2_2 + (dx * cosTheta - dy * sinTheta));
				int y2_3 = (int) Math.round(y2_2 + (dx * sinTheta + dy * cosTheta));
				int x2_4 = (int) Math.round(x2_2 + (dx * cosTheta + dy * sinTheta));
				int y2_4 = (int) Math.round(y2_2 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x2_1, y2_1, x2_2, y2_2, mPaint);
				canvas.drawLine(x2_2, y2_2, x2_3, y2_3, mPaint);
				canvas.drawLine(x2_2, y2_2, x2_4, y2_4, mPaint);
				
				dx = -(int) Math.round(10 / Math.sqrt(2));
				dy = -dx;
				// Вычисления значений для линий (третья группа, x3, y3)
				int x3_1 = (int) Math.round(x + 15 / 2), x3_2 = (int) Math.round(x + 15 / 2 + 20 / Math.sqrt(2));
				int y3_1 = (int) Math.round(y - 15 / 2), y3_2 = (int) Math.round(y - 15 / 2 - 20 / Math.sqrt(2));
				int x3_3 = (int) Math.round(x3_2 + (dx * cosTheta - dy * sinTheta));
				int y3_3 = (int) Math.round(y3_2 + (dx * sinTheta + dy * cosTheta));
				int x3_4 = (int) Math.round(x3_2 + (dx * cosTheta + dy * sinTheta));
				int y3_4 = (int) Math.round(y3_2 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x3_1, y3_1, x3_2, y3_2, mPaint);
				canvas.drawLine(x3_2, y3_2, x3_3, y3_3, mPaint);
				canvas.drawLine(x3_2, y3_2, x3_4, y3_4, mPaint);
				
				dx = -(int) Math.round(10 / Math.sqrt(2));
				dy = dx;
				// Вычисления значений для линий (четвертая группа, x4, y4)
				int x4_1 = (int) Math.round(x + 15 / 2), x4_2 = (int) Math.round(x + 15 / 2 + 20 / Math.sqrt(2));
				int y4_1 = (int) Math.round(y + 15 / 2), y4_2 = (int) Math.round(y + 15 / 2 + 20 / Math.sqrt(2));
				int x4_3 = (int) Math.round(x4_2 + (dx * cosTheta - dy * sinTheta));
				int y4_3 = (int) Math.round(y4_2 + (dx * sinTheta + dy * cosTheta));
				int x4_4 = (int) Math.round(x4_2 + (dx * cosTheta + dy * sinTheta));
				int y4_4 = (int) Math.round(y4_2 - (dx * sinTheta - dy * cosTheta));
				canvas.drawLine(x4_1, y4_1, x4_2, y4_2, mPaint);
				canvas.drawLine(x4_2, y4_2, x4_3, y4_3, mPaint);
				canvas.drawLine(x4_2, y4_2, x4_4, y4_4, mPaint);
			}
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) { // Обработка касаний экрана
		if (react == 1)
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: // ОПУСКАНИЕ ПАЛЬЦА НА ЭКРАН
			dir = "";
			if (event.getPointerCount() == 1) {
				float gx = event.getX(), gy = event.getY();
				if (gx >= width - 55 && gx <= width - 5 && gy >= 5 && gy <= 55) // Необходимость изменения масштаба
				flagm = 1;
				else if (gx >= 5 && gx <= 100 && gy >= 5 && gy <= 50) { // Масштаб по умолчанию
					setScaleGeneral();
				} else if (gx >= width - 170 && gx <= width - 70 && gy >= padd + marg - 55
				&& gy <= padd + marg - 5) { // Показывать ли легенды
					if (showleg == 0)
					showleg = 1;
					else
					showleg = 0;
					i.showleg = showleg;
				} else if (gx >= width - 285 && gx <= width - 185 && gy >= padd + marg - 55
				&& gy <= padd + marg - 5) { // Обнулить фиксации
					HPoints.clear();
					} else { // Поиск точки
					flagm = 0;
					fx = (int) event.getX();
					fy = (int) event.getY();
					findPoint((int) event.getX(), (int) event.getY());
				}
			}
			break;
			case MotionEvent.ACTION_MOVE: // ПРОВЕДЕНИЕ ПАЛЬЦЕМ/ПАЛЬЦАМИ ПО ЭКРАНУ
			dir = "";
			// ПАЛЕЦ 1
			if (event.getPointerCount() == 1 && twofs == 0 && num != 0) {
				// Если нужно выделить точку
				fx = (int) event.getX();
				fy = (int) event.getY();
				findPoint((int) event.getX(), (int) event.getY());
				} else if (event.getPointerCount() == 1 && twofs == 0 && num == 0) {
				//
				//
				//
				//
				//
				//
				// Логика перетаскивания
				if (event.getX() <= width && event.getY() <= height)
				try {
					if (touch1.size() >= 5) {
						// Каждый элемент присваиваем предыдущему
						for (int i = 1; i < 5; i++) {
							touch1.put(i, touch1.get(i + 1)); // Сдвигаем значения
						}
						// Последнему элементу присваиваем координаты касания
						touch1.put(5, new Point(event.getX(), event.getY()));
						} else {
						// Добавляем координаты касания в touch1.size()+1
						touch1.put(touch1.size() + 1, new Point(event.getX(), event.getY()));
					}
					
					// Методом наименьших квадратов сравниваем точки с лучами из 1-ой точки
					Point latestPoint = touch1.get(5); // Последняя точка касания
					if (latestPoint != null) {
						Point firstPoint = touch1.get(1); // Первая точка касания
						float x0 = firstPoint.x, y0 = firstPoint.y;
						float xl = latestPoint.x, yl = latestPoint.y;
						
						String s = "";
						
						// Определяем направление движения
						if (Math.sqrt(Math.pow(yl - y0, 2) + Math.pow(xl - x0, 2)) > 10) {
							float v1 = 0, v2 = 0, v3 = 0, v4 = 0;
							for (int i = 1; i <= touch1.size(); i++) {
								Point p = touch1.get(i);
								float x = p.x, y = p.y;
								// Вертикальная линия
								v1 = v1 + (float) Math
								.pow(Math.abs(1 * x + 0 * y + (-x0)) / Math.sqrt(1 * 1 + 0 * 0), 2); // x-x0 = 0
								// Горизонтальная линия
								v2 = v2 + (float) Math
								.pow(Math.abs(0 * x + 1 * y + (-y0)) / Math.sqrt(0 * 0 + 1 * 1), 2); // y-y0 = 0
								// Диагональные линии
								v3 = v3 + (float) Math.pow(
								Math.abs(1 * x + (-1) * y + (y0 - x0)) / Math.sqrt(1 * 1 + (-1) * (-1)),
								2); // x-y+y0-x0 = 0 - возрастает
								v4 = v4 + (float) Math.pow(
								Math.abs(1 * x + 1 * y + (-y0 - x0)) / Math.sqrt(1 * 1 + 1 * 1), 2); // x+y-y0-x0 = 0 - убывает
							}
							if (v1 <= v2 && v1 <= v3 && v1 <= v4) { // Приоритет v1
								int flag = 0;
								for (int i = 2; i <= touch1.size(); i++) {
									Point p = touch1.get(i);
									Point p2 = touch1.get(i - 1);
									if (p.y > p2.y)
									flag++;
									else
									flag--;
								}
								if (flag > 0)
								s = "Вниз";
								else
								s = "Вверх";
								} else if (v2 <= v1 && v2 <= v3 && v2 <= v4) { // Приоритет v2
								int flag = 0;
								for (int i = 2; i <= touch1.size(); i++) {
									Point p = touch1.get(i);
									Point p2 = touch1.get(i - 1);
									if (p.x > p2.x)
									flag++;
									else
									flag--;
								}
								if (flag > 0)
								s = "Вправо";
								else
								s = "Влево";
								} else if (v3 <= v1 && v3 <= v2 && v3 <= v4) { // Приоритет v3
								int flag = 0;
								for (int i = 2; i <= touch1.size(); i++) {
									Point p = touch1.get(i);
									Point p2 = touch1.get(i - 1);
									if (p.y > p2.y)
									flag++;
									else
									flag--;
								}
								if (flag > 0)
								s = "Вниз вправо";
								else
								s = "Вверх влево";
								} else { // Приоритет v4
								int flag = 0;
								for (int i = 2; i <= touch1.size(); i++) {
									Point p = touch1.get(i);
									Point p2 = touch1.get(i - 1);
									if (p.y > p2.y)
									flag++;
									else
									flag--;
								}
								if (flag > 0)
								s = "Вниз влево";
								else
								s = "Вверх вправо";
							}
							
						}
						
						//
						//
						//
						// Обработка полученной информации о движении пальца
						
						int ix = -10;
						while (Math.abs(x2 - x1) <= Math.pow(10, -ix) * 2)
						ix++;
						float dx = (float) Math.pow(10, -ix);
						int iy = -10;
						while (Math.abs(y2 - y1) <= Math.pow(10, -iy) * 2)
						iy++;
						float dy = (float) Math.pow(10, -iy);
						
						counter++;
						if (counter % 8 == 0) {
							if (s == "Вверх") {
								if (y1 - dy < i.ly1)
								dy = y1 - i.ly1;
								y1 -= dy;
								y2 -= dy;
								} else if (s == "Вверх влево") {
								if (y1 - dy < i.ly1)
								dy = y1 - i.ly1;
								y1 -= dy;
								y2 -= dy;
								if (x2 + dx > i.lx2)
								dx = i.lx2 - x2;
								x1 += dx;
								x2 += dx;
								} else if (s == "Вверх вправо") {
								if (y1 - dy < i.ly1)
								dy = y1 - i.ly1;
								y1 -= dy;
								y2 -= dy;
								if (x1 - dx < i.lx1)
								dx = x1 - i.lx1;
								x1 -= dx;
								x2 -= dx;
								} else if (s == "Вниз") {
								if (y2 + dy > i.ly2)
								dy = i.ly2 - y2;
								y1 += dy;
								y2 += dy;
								} else if (s == "Вниз влево") {
								if (y2 + dy > i.ly2)
								dy = i.ly2 - y2;
								y1 += dy;
								y2 += dy;
								if (x2 + dx > i.lx2)
								dx = i.lx2 - x2;
								x1 += dx;
								x2 += dx;
								} else if (s == "Вниз вправо") {
								if (y2 + dy > i.ly2)
								dy = i.ly2 - y2;
								y1 += dy;
								y2 += dy;
								if (x1 - dx < i.lx1)
								dx = x1 - i.lx1;
								x1 -= dx;
								x2 -= dx;
								} else if (s == "Вправо") {
								if (x1 - dx < i.lx1)
								dx = x1 - i.lx1;
								x1 -= dx;
								x2 -= dx;
								} else if (s == "Влево") {
								if (x2 + dx > i.lx2)
								dx = i.lx2 - x2;
								x1 += dx;
								x2 += dx;
							}
							x1 = (float) (Math.round(x1 * Math.pow(10, ix)) / Math.pow(10, ix));
							x2 = (float) (Math.round(x2 * Math.pow(10, ix)) / Math.pow(10, ix));
							y1 = (float) (Math.round(y1 * Math.pow(10, iy)) / Math.pow(10, iy));
							y2 = (float) (Math.round(y2 * Math.pow(10, iy)) / Math.pow(10, iy));
							i.x1 = x1;
							i.x2 = x2;
							i.y1 = y1;
							i.y2 = y2;
						}
					}
					} catch (Exception e) {
					errors = e.getMessage();
				}
				
				} else if (event.getPointerCount() == 2) {
				// ОБРАБОТКА ДВИЖЕНИЙ 2 ПАЛЬЦЕВ
				num = 0; // Сброс выделенной точки
				hx = 0;
				fx = 0;
				hy = 0;
				fy = 0;
				//
				//
				//
				//
				//
				//
				// Масштабирование двумя пальцами
				if (event.getX(event.getPointerId(0)) <= width && event.getY(event.getPointerId(0)) <= height
				&& event.getX(event.getPointerId(1)) <= width
				&& event.getY(event.getPointerId(1)) <= height)
				try {
					if (touch2.size() >= 5) {
						// Каждый элемент присваиваем предыдущему
						for (int i = 1; i < 5; i++) {
							touch2.put(i, touch2.get(i + 1)); // Сдвигаем значения
						}
						// 5-ому элементу присваиваем координаты касания
						touch2.put(5, new Point(event.getX(event.getPointerId(0)),
						event.getY(event.getPointerId(0))));
						} else {
						// Добавляем координаты касания в touch1.size()+1
						touch2.put(touch2.size() + 1, new Point(event.getX(event.getPointerId(0)),
						event.getY(event.getPointerId(0))));
					}
					if (touch3.size() >= 5) {
						// Каждый элемент присваиваем предыдущему
						for (int i = 1; i < 5; i++) {
							touch3.put(i, touch3.get(i + 1)); // Сдвигаем значения
						}
						// 5-ому элементу присваиваем координаты касания
						touch3.put(5, new Point(event.getX(event.getPointerId(1)),
						event.getY(event.getPointerId(1))));
						} else {
						// Добавляем координаты касания в touch1.size()+1
						touch3.put(touch3.size() + 1, new Point(event.getX(event.getPointerId(1)),
						event.getY(event.getPointerId(1))));
					}
					
					String s1 = "", s2 = "";
					// Методом наименьших квадратов сравниваем точки с лучами из 1-ой точки
					Point latestPoint = touch2.get(5); // Последняя точка касания
					Point firstPoint = touch2.get(1); // Первая точка касания
					float x0 = firstPoint.x, y0 = firstPoint.y;
					String s = "";
					
					float xl = 0, yl = 0;
					if (latestPoint != null) {
						xl = latestPoint.x;
						yl = latestPoint.y;
					}
					float r1 = (float) Math.sqrt(Math.pow(yl - y0, 2) + Math.pow(xl - x0, 2));
					if (r1 <= 10)
					s = "На месте";
					// Определяем направление движения
					// ПЕРВЫЙ ПАЛЕЦ
					if (latestPoint != null && r1 > 10) {
						float v1 = 0, v2 = 0, v3 = 0, v4 = 0;
						for (int i = 1; i <= touch2.size(); i++) {
							Point p = touch2.get(i);
							float x = p.x, y = p.y;
							// Вертикальная линия
							v1 = v1 + (float) Math
							.pow(Math.abs(1 * x + 0 * y + (-x0)) / Math.sqrt(1 * 1 + 0 * 0), 2); // x-x0 = 0
							// Горизонтальная линия
							v2 = v2 + (float) Math
							.pow(Math.abs(0 * x + 1 * y + (-y0)) / Math.sqrt(0 * 0 + 1 * 1), 2); // y-y0 = 0
							// Диагональные линии
							v3 = v3 + (float) Math.pow(
							Math.abs(1 * x + (-1) * y + (y0 - x0)) / Math.sqrt(1 * 1 + (-1) * (-1)), 2); // x-y+y0-x0 = 0 - возрастает
							v4 = v4 + (float) Math
							.pow(Math.abs(1 * x + 1 * y + (-y0 - x0)) / Math.sqrt(1 * 1 + 1 * 1), 2); // x+y-y0-x0 = 0 - убывает
						}
						if (v1 <= v2 && v1 <= v3 && v1 <= v4) { // Приоритет v1
							int flag = 0;
							for (int i = 2; i <= touch2.size(); i++) {
								Point p = touch2.get(i);
								Point p2 = touch2.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз";
							else
							s = "Вверх";
							} else if (v2 <= v1 && v2 <= v3 && v2 <= v4) { // Приоритет v2
							int flag = 0;
							for (int i = 2; i <= touch2.size(); i++) {
								Point p = touch2.get(i);
								Point p2 = touch2.get(i - 1);
								if (p.x > p2.x)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вправо";
							else
							s = "Влево";
							} else if (v3 <= v1 && v3 <= v2 && v3 <= v4) { // Приоритет v3
							int flag = 0;
							for (int i = 2; i <= touch2.size(); i++) {
								Point p = touch2.get(i);
								Point p2 = touch2.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз вправо";
							else
							s = "Вверх влево";
							} else { // Приоритет v4
							int flag = 0;
							for (int i = 2; i <= touch2.size(); i++) {
								Point p = touch2.get(i);
								Point p2 = touch2.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз влево";
							else
							s = "Вверх вправо";
						}
						
					}
					s1 = s;
					
					// ВТОРОЙ ПАЛЕЦ
					// Методом наименьших квадратов сравниваем точки с лучами из 1-ой точки
					latestPoint = touch3.get(5); // Последняя точка касания
					firstPoint = touch3.get(1); // Первая точка касания
					x0 = firstPoint.x;
					y0 = firstPoint.y;
					s = "";
					if (latestPoint != null) {
						xl = latestPoint.x;
						yl = latestPoint.y;
					}
					float r2 = (float) Math.sqrt(Math.pow(yl - y0, 2) + Math.pow(xl - x0, 2));
					if (r2 <= 10)
					s = "На месте";
					// Определяем направление движения
					if (latestPoint != null && r2 > 10) {
						float v1 = 0, v2 = 0, v3 = 0, v4 = 0;
						for (int i = 1; i <= touch3.size(); i++) {
							Point p = touch3.get(i);
							float x = p.x, y = p.y;
							// Вертикальная линия
							v1 = v1 + (float) Math
							.pow(Math.abs(1 * x + 0 * y + (-x0)) / Math.sqrt(1 * 1 + 0 * 0), 2); // x-x0 = 0
							// Горизонтальная линия
							v2 = v2 + (float) Math
							.pow(Math.abs(0 * x + 1 * y + (-y0)) / Math.sqrt(0 * 0 + 1 * 1), 2); // y-y0 = 0
							// Диагональные линии
							v3 = v3 + (float) Math.pow(
							Math.abs(1 * x + (-1) * y + (y0 - x0)) / Math.sqrt(1 * 1 + (-1) * (-1)), 2); // x-y+y0-x0 = 0 - возрастает
							v4 = v4 + (float) Math
							.pow(Math.abs(1 * x + 1 * y + (-y0 - x0)) / Math.sqrt(1 * 1 + 1 * 1), 2); // x+y-y0-x0 = 0 - убывает
						}
						if (v1 <= v2 && v1 <= v3 && v1 <= v4) { // Приоритет v1
							int flag = 0;
							for (int i = 2; i <= touch3.size(); i++) {
								Point p = touch3.get(i);
								Point p2 = touch3.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз";
							else
							s = "Вверх";
							} else if (v2 <= v1 && v2 <= v3 && v2 <= v4) { // Приоритет v2
							int flag = 0;
							for (int i = 2; i <= touch3.size(); i++) {
								Point p = touch3.get(i);
								Point p2 = touch3.get(i - 1);
								if (p.x > p2.x)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вправо";
							else
							s = "Влево";
							} else if (v3 <= v1 && v3 <= v2 && v3 <= v4) { // Приоритет v3
							int flag = 0;
							for (int i = 2; i <= touch3.size(); i++) {
								Point p = touch3.get(i);
								Point p2 = touch3.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз вправо";
							else
							s = "Вверх влево";
							} else { // Приоритет v4
							int flag = 0;
							for (int i = 2; i <= touch3.size(); i++) {
								Point p = touch3.get(i);
								Point p2 = touch3.get(i - 1);
								if (p.y > p2.y)
								flag++;
								else
								flag--;
							}
							if (flag > 0)
							s = "Вниз влево";
							else
							s = "Вверх вправо";
						}
						
					}
					s2 = s;
					// ОПРЕДЕЛЕНИЕ ЗНАЧЕНИЯ ДВИЖЕНИЙ ДВУХ ПАЛЬЦЕВ
					if (latestPoint != null) {
						int flag = 0, flag2 = 0;
						
						// Вертикальное сжатие
						if ((s1.equals("Вверх") && s2.equals("Вниз") && touch2.get(1).y > touch3.get(1).y)
						|| (s2.equals("Вверх") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y)
						|| (s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y)
						|| (s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y)
						|| (s1.equals("Вверх") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y)
						|| (s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y)) {
							flag2--; // Уменьшение по вертикали
						}
						
						// Горизонтальное сжатие
						else if ((s1.equals("Влево") && s2.equals("Вправо")
						&& touch2.get(1).x > touch3.get(1).x)
						|| (s2.equals("Влево") && s1.equals("Вправо")
						&& touch2.get(1).x < touch3.get(1).x)
						|| (s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).x > touch3.get(1).x)
						|| (s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).x < touch3.get(1).x)
						|| (s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x)
						|| (s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x)) {
							flag--; // Уменьшение по горизонтали
						}
						
						// Вертикальное растяжение
						else if ((s1.equals("Вверх") && s2.equals("Вниз") && touch2.get(1).y < touch3.get(1).y)
						|| (s2.equals("Вверх") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y)
						|| (s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y)
						|| (s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y)
						|| (s1.equals("Вверх") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y)
						|| (s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y)) {
							flag2++; // Увеличение по вертикали
						}
						
						// Горизонтальное растяжение
						else if ((s1.equals("Влево") && s2.equals("Вправо")
						&& touch2.get(1).x < touch3.get(1).x)
						|| (s2.equals("Влево") && s1.equals("Вправо")
						&& touch2.get(1).x > touch3.get(1).x)
						|| (s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).x < touch3.get(1).x)
						|| (s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).x > touch3.get(1).x)
						|| (s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x)
						|| (s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x)) {
							flag++; // Увеличение по горизонтали
						}
						
						// Диагональное сжатие
						else if (s1.equals("Вверх") && s2.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх") && s2.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Влево") && s2.equals("Вверх вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Влево") && s2.equals("Вниз вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("Вверх влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("Вниз влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вверх") && s1.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх") && s1.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Влево") && s1.equals("Вверх вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Влево") && s1.equals("Вниз вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("Вверх влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("Вниз влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Вверх") && s2.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x) {
							flag--;
							flag2--;
						}
						
						// Диагональное растяжение
						else if (s1.equals("Вверх") && s2.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх") && s2.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Влево") && s2.equals("Вверх вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Влево") && s2.equals("Вниз вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("Вверх влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("Вниз влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вверх") && s1.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх") && s1.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Влево") && s1.equals("Вверх вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Влево") && s1.equals("Вниз вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("Вверх влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("Вниз влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вниз")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("На месте") && s2.equals("Вверх вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз вправо")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вверх влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("На месте") && s2.equals("Вниз влево")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вниз")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Влево")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вправо")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("На месте") && s1.equals("Вверх вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз вправо")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вверх влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("На месте") && s1.equals("Вниз влево")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Вверх") && s2.equals("Вниз влево")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх влево") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вверх вправо") && s2.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз влево") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Вниз вправо") && s2.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Влево") && s2.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s1.equals("Вправо") && s2.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх влево") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вверх вправо") && s1.equals("На месте")
						&& touch2.get(1).y > touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз влево") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Вниз вправо") && s1.equals("На месте")
						&& touch2.get(1).y < touch3.get(1).y
						|| s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Влево") && s1.equals("На месте")
						&& touch2.get(1).x > touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x
						|| s2.equals("Вправо") && s1.equals("На месте")
						&& touch2.get(1).x < touch3.get(1).x) {
							flag++;
							flag2++;
						}
						
						// Обработка типа движения
						// Флаги из памяти
						if (flag_d == 0) {
							if (flag_v == 1)
							flag = 0;
							if (flag_h == 1)
							flag2 = 0;
							} else {
							if (flag == 0)
							flag = flag2;
							if (flag2 == 0)
							flag2 = flag;
						}
						// Определение флага, означающего тип движения
						if (flag_h == 0 && flag_v == 0 && flag_d == 0) {
							if (flag != 0 && flag2 == 0)
							flag_h = 1;
							if (flag == 0 && flag2 != 0)
							flag_v = 1;
							if (flag != 0 && flag2 != 0)
							flag_d = 1;
						}
						if (flag == 0 && flag2 < 0)
						dir = "Вертикальное сжатие";
						if (flag < 0 && flag2 == 0)
						dir = "Горизонтальное сжатие";
						if (flag < 0 && flag2 < 0)
						dir = "Диагональное сжатие";
						if (flag == 0 && flag2 > 0)
						dir = "Вертикальное растяжение";
						if (flag > 0 && flag2 == 0)
						dir = "Горизонтальное растяжение";
						if (flag > 0 && flag2 > 0)
						dir = "Диагональное растяжение";
						
						// Обработка полученного движения
						// МАСШТАБИРОВАНИЕ ГРАФИКА
						int iy = -10;
						while (Math.abs(y2 - y1) <= Math.pow(10, -iy) * 2)
						iy++;
						int ix = -10;
						while (Math.abs(x2 - x1) <= Math.pow(10, -ix) * 2)
						ix++;
						float dx = (float) Math.pow(10, -ix) / 2;
						float dy = (float) Math.pow(10, -iy) / 2;
						// Определяем минимальный разрыв по осям ох и оу
						float mindx = x2 - x1, mindy = y2 - y1;
						for (int j = 1; j < data.size(); j++) { // Проходим по всем массивам данных
							Line l0 = data.get(j);
							HashMap<Integer, Double> t = l0.time;
							float t0 = (float) (t.get(2) - t.get(1));
							if (t0 < mindx)
							mindx = t0;
							HashMap<Integer, Double> d = l0.data;
							for (int k = 2; k < d.size(); k++) {
								float d0 = (float) Math.abs(d.get(k) - d.get(k - 1));
								if (d0 < mindy)
								mindy = d0;
							}
						}
						//Сичтаем до прохождения пальцем 8 точек
						counter++;
						if (counter % 8 == 0) {
							// Меняем в зависимости от срединной точки xx, yy
							float x = xx; // Точка X, которую мы центруем
							float y = yy; // Точка Y, которую мы центруем
							
							// Ось OX
							float extentX; // Переменная для хранения изменений по оси X
							if ((x2 - x) > (x - x1)) { // Отрезок справа от точки больше
								extentX = (x2 - x) - (x - x1); // Рассчитываем разницу
								if (flag > 0) {
									// Уменьшаем отрезки
									x2 -= extentX; // Уменьшаем правую границу
									} else if (flag < 0) {
									// Увеличиваем отрезки
									if (x1 - extentX >= i.lx1) {
										x1 -= extentX; // Уменьшаем левую границу, если не превышаем минимальную
										} else {
										// Если ограничение не позволяет уменьшить x1, увеличиваем x2
										x2 += dx; // Увеличиваем x2
									}
								}
								} else if ((x2 - x) < (x - x1)) { // Отрезок слева от точки больше
								extentX = (x - x1) - (x2 - x); // Рассчитываем разницу
								if (flag > 0) {
									// Уменьшаем отрезок
									x1 += extentX; // Увеличиваем левую границу
									} else if (flag < 0) {
									// Увеличиваем отрезки
									if (x2 + extentX <= i.lx2) {
										x2 += extentX; // Увеличиваем правую границу, если не превышаем максимальную
										} else {
										// Если ограничение не позволяет увеличить x2, уменьшаем x1
										x1 -= dx; // Уменьшаем x1
									}
								}
								} else { // Отрезки равны
								if (flag > 0) {
									// Увеличиваем пространство
									if (dx >= mindx) {
										x1 += dx;
										x2 -= dx;
									}
									} else if (flag < 0) {
									// Уменьшаем пространство
									x1 -= dx;
									x2 += dx;
								}
							}
							
							// Проверяем, чтобы x1 и x2 оставались в заданных пределах
							if (x1 < i.lx1) {
								x1 = i.lx1; // Устанавливаем x1 в lx1
							}
							if (x2 > i.lx2) {
								x2 = i.lx2; // Устанавливаем x2 в lx2
							}
							
							// Ось OY
							float extentY; // Переменная для хранения изменений по оси Y
							if ((y2 - y) > (y - y1)) { // Отрезок выше от точки больше
								extentY = (y2 - y) - (y - y1); // Рассчитываем разницу
								if (flag2 > 0) {
									// Уменьшаем отрезки
									y2 -= extentY; // Уменьшаем верхнюю границу
									} else if (flag2 < 0) {
									// Увеличиваем отрезки
									if (y1 - extentY >= i.ly1) {
										y1 -= extentY; // Уменьшаем нижнюю границу, если не превышаем минимальную
										} else {
										// Если ограничение не позволяет уменьшить y1, увеличиваем y2
										y2 += dy; // Увеличиваем y2
									}
								}
								} else if ((y2 - y) < (y - y1)) { // Отрезок ниже от точки больше
								extentY = (y - y1) - (y2 - y); // Рассчитываем разницу
								if (flag2 > 0) {
									// Уменьшаем отрезок
									y1 += extentY; // Увеличиваем нижнюю границу
									} else if (flag2 < 0) {
									// Увеличиваем отрезки
									if (y2 + extentY <= i.ly2) {
										y2 += extentY; // Увеличиваем верхнюю границу, если не превышаем максимальную
										} else {
										// Если ограничение не позволяет увеличить y2, уменьшаем y1
										y1 -= dy; // Уменьшаем y1
									}
								}
								} else { // Отрезки равны
								if (flag2 > 0) {
									// Увеличиваем пространство
									if (dy >= mindy) {
										y1 += dy;
										y2 -= dy;
									}
									} else if (flag2 < 0) {
									// Уменьшаем пространство
									y1 -= dy;
									y2 += dy;
								}
							}
							
							// Проверяем, чтобы y1 и y2 оставались в заданных пределах
							if (y1 < i.ly1) {
								y1 = i.ly1; // Устанавливаем y1 в ly1
							}
							if (y2 > i.ly2) {
								y2 = i.ly2; // Устанавливаем y2 в ly2
							}
							
							i.x1 = x1;
							i.x2 = x2;
							i.y1 = y1;
							i.y2 = y2;
						}
					}
					if (twofs == 0) {
						// Если только началось касание двух пальцев, запоминаем среднюю точку, около которой надо изменять масштаб
						xx = index_to_x(Math.round((touch2.get(1).x + touch3.get(1).x) / 2));
						yy = index_to_y(Math.round((touch2.get(1).y + touch3.get(1).y) / 2));
					}
					twofs = 1;
					} catch (Exception e) {
					errors = e.getMessage();
				}
			}
			break;
			case MotionEvent.ACTION_UP:
			//
			//
			//
			//
			//
			//
			// ПАЛЕЦ БЫЛ ПОДНЯТ
			// Обнуляем всё, что нужнг
			dir = "";
			xx = 0;
			yy = 0;
			flag_v = 0;
			flag_h = 0;
			flag_d = 0;
			twofs = 0;
			touch1.clear();
			touch2.clear();
			touch3.clear();
			num = 0; // Сброс выделенной точки
			hx = 0;
			fx = 0;
			hy = 0;
			fy = 0;
			break;
		}
		invalidate(); // Перерисовка графика
		return true;
	}
	
	public void setScaleGeneral() { // Сбросить масштаб графика до масштаба по умолчанию
		x1 = i.lx2 - window;
		x2 = i.lx2;
		y1 = i.ly1;
		y2 = i.ly2;
		i.x1 = x1;
		i.x2 = x2;
		i.y1 = y1;
		i.y2 = y2;
		invalidate();
	}
}