package Lab2;

import java.util.Scanner;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class main {

    public static void main (String[] args) {
		CLI prog = new CLI();
		
		if (!prog.getInfo())
		 	System.exit(-1);
		
		prog.start();
		
		prog.finish();
		
		System.exit(0);
    }
}