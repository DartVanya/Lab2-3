package Lab3;

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import Lab3.Beans.*;

// Класс, работающий с панелью, генерирует, печатает панель, осуществляет нажатие кнопки
public class ControlPanel extends CLI implements Runnable {

    private int N, M;
    private item items[][];
    private Vector<Point> Lamp_arr = new Vector<Point>();
    private Vector<Point> Butt_arr = new Vector<Point>();
    private Vector<Vector<Integer>> but_to_lamp;
    private Random rand = new Random();
    
    // Конструктор панели
    protected ControlPanel (int n, int m, AnnotationConfigApplicationContext ctx) {
    	N = n; M = m;
        items = new item[N][M];
        int pos;
    	boolean type;
    	
        // Генерируем панель, размещение кнопок и ламп случайно
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
            	type = rand.nextBoolean();
                if (type) // Получаем бины для лампы или для кнопки
                	items[i][j] = ctx.getBean("Lamp", item.class);
                else
                	items[i][j] = ctx.getBean("Button", item.class);
            }
        }
        
        // Собираем кнопки и лампы в вектор
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
            	if (items[i][j].get_type())
            		Butt_arr.add(new Point(i, j));
            	else
            		Lamp_arr.add(new Point(i, j));
            }
        }
        
        // Временные переменные для упрощения кода
        int l_cnt = Lamp_arr.size();
        int b_cnt = Butt_arr.size();
        
        but_to_lamp = new Vector<Vector<Integer>>(l_cnt);
        
        // Привязываем случайное число ламп к каждой кнопке, порядок случаен
        for (int i = 0; i < b_cnt && l_cnt > 0; i++) {
        	but_to_lamp.add(i, new Vector<Integer>());
        	pos = rand.nextInt(l_cnt); // случайное число ламп
            for (int r = 0; r < pos; r++) { // случайный порядок ламп
            	but_to_lamp.get(i).add(rand.nextInt(l_cnt));
            }
        }

    }
    
    // Вывод панели на экран
    protected void print() {
    	for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
            	items[i][j].print();
            	if(j < M - 1)
            		System.out.print(" - ");
            }
            System.out.println();
        }
        System.out.println();
    }
    
    // Получение индекса из массива по координатам 
    private int get_index(Vector<Point> vec, int x, int y) {
    	  for (int i = 0; i < vec.size(); i++) {
    		  Point temp = vec.get(i);
    		  if(temp.x == x && temp.y == y)
    			  return i;
    	  }
    	  return -1;
    }
   
    
    // Метод нажатия кнопки
    protected void PressButton(int X, int Y) {
    	
    	// Получаем индекс кнопки по координатам
    	int res = this.get_index(Butt_arr, X, Y);
    	if(res == -1) {
    		 System.out.println("Ошибка! Это не кнопка! Введите заново!");
    		 return;
    	}
    	else if(Lamp_arr.size() < 1) {
    		items[X][Y].swith();
			return;
    	}

        int index;
        Point temp;
    	for (int i = 0; i < but_to_lamp.get(res).size(); i++) { // Перебираем индексы ламп, привязанной к этой кнопке
    		index = but_to_lamp.get(res).get(i); // Поочереди получаем индексы ламп из массива
            temp = new Point(Lamp_arr.get(index).x, Lamp_arr.get(index).y); // Временная переменная для индексов и координат
            items[temp.x][temp.y].swith(); // Включаем
    	}
    	items[X][Y].swith();
    }
    
	private final Semaphore sem = new Semaphore(1); // даём одно разрешение, если дать больше, то будет конфликт
	// можно попробовать поставить больше 1 и посмотреть, как ломается вывод
	
    // Метод работающий в потоке
    public void run() {
    	int r_bi = 0;
    	boolean Off = true;
		try {
			sem.acquire();
			while(true) {
				if (Thread.currentThread().isInterrupted()) break;
				// Осуществляем переключение случайной кнопки на панели и выводим её на экран
				if (Off)
					r_bi = Butt_arr.size() > 1 ? rand.nextInt(Butt_arr.size() - 1) : 0;
				if (Butt_arr.size() > 0)
					this.PressButton(Butt_arr.get(r_bi).x, Butt_arr.get(r_bi).y);
				else
					System.out.println("Ошибка! Это не кнопка!");
				Off = !Off;
				Thread.sleep(1); // фикс, чтобы не баговал первый print()
				this.print();
				System.out.println(this.info);
				
				try {
					Thread.sleep(this.Timeout); // переключение выполняется раз в Timeout мсек
				} catch (InterruptedException e) { 
					// System.out.println("Поток остановлен!");
					break;
				}
			}
			sem.release();
		}
		catch(InterruptedException e)
        {
			  // System.out.println("Поток остановлен!");
        }
    }
}