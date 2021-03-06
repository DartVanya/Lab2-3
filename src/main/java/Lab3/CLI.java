package Lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import Lab3.Beans.SpringConfig;


// Интерфейс командной строки, взаимодействует с пользователем
public class CLI {
	private int N, M, x, y;
	private Scanner in;
	private AnnotationConfigApplicationContext ctx;
	private ControlPanel panel;
	protected String info = "Команды: -1 - выход, -2 - переключить режим моргания панели\nНажмите кнопку:";
	private boolean AutoButtonsPress = false;
	protected int Timeout = 2000;
	
	// Конструктор по умолчанию
	public CLI() {}
    
	// Конструктор с параметрами
	public CLI (boolean AutoPress, int tout) {
		 AutoButtonsPress = AutoPress;
		 Timeout = tout;
	}
	
	// Получаем информацию о размере панели
	public boolean getInfo() {
	    in = new Scanner(System.in);
	    System.out.println("Введите параметры панели: ");
	    N = in.nextInt();
	    M = in.nextInt();
	    if (N < 1 || M < 1) {
	       System.out.println("Вы ввели неправильный размер панели! Завершение работы программы...");
	       return false;
	    }
	    return true;
	}
	
	private List<Thread> lt = new ArrayList<>();
	private int n = 3; // количество запускаемых потоков
	
	// Запускаем процесс взаимодействия с пользователем
	public void start() {
		ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		panel = new ControlPanel(M, N, ctx);
        System.out.println("Сгенерирована панель управления.");
        panel.print();
        
        // Создаем новые потоки и запускаем их, если включено автопереключение
        if (AutoButtonsPress) {
			for (int i = 0; i < n; i++)
				lt.add(new Thread(panel));
			for (Thread thread : lt)
				thread.start();
        }
        
        // Запрос данных у пользователя
        while(true) {
        	 System.out.println(info);
        	 x = in.nextInt();
             if (x == -1) break;
             else if (x == -2) { // Переключение мигания панели
            	 this.SwitchAutoPress();
            	 continue;
             }
             y = in.nextInt();
             if (y == -1) break;
             else if (y == -2) {
            	 this.SwitchAutoPress();
            	 continue; 	 
             }
             if (x < 0 || x >= N || y < 0 || y >= M) {
                 System.out.println("Вы ввели неправильные координаты! Введите ещё раз.");
                 continue;
             }
             // Нажимаем кнопку и выводим панель
             panel.PressButton(M-1-y, x);
             panel.print(); 
        }
	}
	
	// Метод переключения автонажатия
	void SwitchAutoPress() {
		 if (AutoButtonsPress) { // прерываем потоки, если они былм активены
			for (Thread thread : lt) 
				thread.interrupt();
		 } 
		 else { // или создаём новые и запускаем
			lt.clear();
			for (int i = 0; i < n; i++) 
				lt.add(new Thread(panel));
			for (Thread thread : lt)
				thread.start();
		 }
		 AutoButtonsPress = !AutoButtonsPress;
		 System.out.println("Режим автопереключения " + (AutoButtonsPress ? "включен" : "выключен") + ". Ура!");
	}
	
	// Завершаем работу
	public void finish() {
        if (AutoButtonsPress) { // завершаем потоки по окончании выполнения программы
			for (Thread thread : lt)
				thread.interrupt();
        }	 
		ctx.close();
		System.out.println("Завершение работы программы...");
	}
}
