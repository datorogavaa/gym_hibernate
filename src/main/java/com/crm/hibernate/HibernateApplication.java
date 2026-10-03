package com.crm.hibernate;

import com.crm.hibernate.config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class HibernateApplication {

	public static void main(String[] args) {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
			EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
			EntityManager em = emf.createEntityManager();

			System.out.println("\n--- USERS IN H2 ---");
			List<?> users = em.createQuery("SELECT u FROM User u").getResultList();
			users.forEach(System.out::println);

			System.out.println("\n--- TRAINING TYPES IN H2 ---");
			List<?> types = em.createQuery("SELECT t FROM TrainingType t").getResultList();
			types.forEach(System.out::println);

			em.close();
		}	}

}
