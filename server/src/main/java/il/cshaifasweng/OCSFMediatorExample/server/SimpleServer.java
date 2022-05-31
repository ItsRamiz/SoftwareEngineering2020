package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.*;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.WorkerUpdateManager;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ManagerUpdateManager;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.OrderUpdateManager;


//import il.cshaifasweng.OCSFMediatorExample.server.Product;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.EntityManager;
import javax.persistence.PreUpdate;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


public class SimpleServer extends AbstractServer {

	public static Session session;
	private static Session session1;
	private List<Product> productGeneralList = new ArrayList<Product>();
	private List<Account> accountGeneralList = new ArrayList<Account>();
	private int flowersnum = 0;

	public SimpleServer(int port) {
		super(port);

	}

	public void Saveinsess() {
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		for (int i = 0; i < productGeneralList.size(); i++) {

			session.save(productGeneralList.get(i)); // save the Product in the database
			session.flush();
		}
		session.close();
	}

	public static SessionFactory getSessionFactory() throws HibernateException {
		Configuration configuration = new Configuration();

		// Add ALL of your entities here. You can also try adding a whole package.
		configuration.addAnnotatedClass(Product.class);
		configuration.addAnnotatedClass(Account.class);
		configuration.addAnnotatedClass(Worker.class);
		configuration.addAnnotatedClass(Manager.class);
		configuration.addAnnotatedClass(Order.class);


		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.build();

		return configuration.buildSessionFactory(serviceRegistry);
	}

	public static void generateProducts() {
		System.out.println("arrived to generate products function");
		Product product = new Product(5, "btn", "flower1", "someDetails", "5000");
		System.out.println("finisehd creating the product");
		session.save(product);

		/** The call to session.flush() updates the DB immediately without ending the transaction.
		 * Recommended to do after an arbitrary unit of work.
		 * MANDATORY to do if you are saving a large amount of data - otherwise you may get
		 cache errors.*/

		session.flush();
	}

	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) throws SQLException, IOException {

		System.out.println("handleeeeeeeeeeee");
		System.out.println("msg class is " +msg.getClass());
		if (msg instanceof String) {
			SessionFactory sessionFactory = getSessionFactory();
			session = sessionFactory.openSession();
			Transaction tx1 = session.beginTransaction();

			String recievedStr = (String) msg;
			if (recievedStr.equals("first entry")) { // if arrived here it means we opened the app
				System.out.println("entered first entry");
				List<String> list = session.createSQLQuery("SHOW TABLES from flowers;").list();


				System.out.println(list.get(0));
				System.out.println(list.get(1));

				//System.out.println("the number of rows of products table is:" + countRows());
				int tableFoundIndex = -1;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i).equals("products_table")) {
						tableFoundIndex = i; // save the index of the table "products_table"
					}
				}
				if (tableFoundIndex != -1) { // means we found a table
					if (countRows() == 0) { // if found the table, ask if it has more than 0 rows
						System.out.println("didnt find a table (this message is from the server");
						client.sendToClient("not found");
						session.close();
					} else {
						List<Product> resultList = getAllProducts();

						FoundTable foundTbl = new FoundTable("found", resultList);
						client.sendToClient(foundTbl); // if found the table then tell the client, so they know they dont intialize 6 products again
						session.close();
					}
				} else {

					client.sendToClient("not found");
					session.close();
				}
			}


		}

		if (msg instanceof UpdateMessage) {
			SessionFactory sessionFactory = getSessionFactory();
			session = sessionFactory.openSession();
			Transaction tx1 = session.beginTransaction();

			UpdateMessage recievedMessage = (UpdateMessage) msg;
			String updateClassName = recievedMessage.getUpdateClass();
			String updateClassFunction = recievedMessage.getUpdateFunction();

			switch (updateClassName) { // checks which class to be updated
				case "product":

					if (updateClassFunction.equals("add")) {
						System.out.println("arrived to here inside add");
						Product recievedProd = recievedMessage.getProduct();
						addItemToCatalog(recievedProd);

					} else if (updateClassFunction.equals("remove")) {
						String idToRemove = recievedMessage.getDelteId();
						removeItemFromCatalog(idToRemove, client);
					}
					else if(updateClassFunction.equals("edit")){
						System.out.println("Arrived edit case in the switch !");
						Product recievedProd = recievedMessage.getProduct();
						editCatalogProduct(recievedProd);
					}

					session.close();
					break;


				case "account":
					if (updateClassFunction.equals("add")) {
						System.out.println("arrived to here inside add");
						Account NewAcc = recievedMessage.getAccount();
						addAccount(NewAcc);

					} else if (updateClassFunction.equals("remove")) {
						String idToRemove = recievedMessage.getDelteId();
						removeAccount(idToRemove, client);
					}
					else if(updateClassFunction.equals("edit")){
						System.out.println("Arrived edit account case in switch !");
						Account editAcc = recievedMessage.getAccount();
						editAccount(editAcc);
					}

					break;

				case "worker":
					if (updateClassFunction.equals("add")) {
						System.out.println("arrived to here inside worker add");
						Worker recievedWorker = recievedMessage.getWorker();
						WorkerUpdateManager.addWorker(recievedWorker);

					} else if (updateClassFunction.equals("remove")) {
						String idToRemove = recievedMessage.getDelteId();
						WorkerUpdateManager.removeWorker(idToRemove, client);
					}
					else if(updateClassFunction.equals("edit")){
						System.out.println("Arrived edit worker case in switch !");
						Worker recievedWorker = recievedMessage.getWorker();
						WorkerUpdateManager.editWorker(recievedWorker);
					}

					session.close();

					break;

				case "manager":
					if (updateClassFunction.equals("add")) {
						System.out.println("arrived to here inside manager add");
						Manager recievedManager = recievedMessage.getManager();
						ManagerUpdateManager.addManager(recievedManager);

					} else if (updateClassFunction.equals("remove")) {
						String idToRemove = recievedMessage.getDelteId();
						ManagerUpdateManager.removeManager(idToRemove, client);
					}
					else if(updateClassFunction.equals("edit")){
						System.out.println("Arrived edit manager case in switch !");
						Manager recievedManager = recievedMessage.getManager();
						ManagerUpdateManager.editManager(recievedManager);
					}

					session.close();

					break;

				case "order":
					if (updateClassFunction.equals("add")) {
						System.out.println("arrived to here inside order add");
						Order recievedOrder = recievedMessage.getOrder();
						OrderUpdateManager.addOrder(recievedOrder);

					} else if (updateClassFunction.equals("remove")) {
						String idToRemove = recievedMessage.getDelteId();
						OrderUpdateManager.removeOrder(idToRemove, client);
					}
					/*else if(updateClassFunction.equals("edit")){
						System.out.println("Arrived edit order case in switch !");
						Order recievedOrder = recievedMessage.getOrder();
						OrderUpdateManager.editOrder(recievedOrder);
					}*/

					session.close();

					break;

				case "cart":


					break;

			}
		}

		if(msg instanceof CheckMail){

			SessionFactory sessionFactory = getSessionFactory();
			session = sessionFactory.openSession();
			Transaction tx1 = session.beginTransaction();

			CheckMail recievedMessage = (CheckMail) msg;
			String recievedMailStr = recievedMessage.getEmail();
			String recievedPasswordStr = recievedMessage.getPassword();

			boolean foundTheMail = false ;
			boolean foundThePassword = false ;
			// 1. apply query to check if the mail exists in the accounts table
			List<Account> accountsList = getAllAccounts();
			for (int i=0;i<accountsList.size();i++){
				System.out.println(accountsList.get(i).getEmail());
				if(accountsList.get(i).getEmail().equals(recievedMailStr)){
					foundTheMail = true ;
					System.out.println("found the mail ! the iteration is: " + i);
				}

			}
			// 2. if the account exists then check if the password matches
			if(foundTheMail){
				for (int i=0;i<accountsList.size();i++){
					System.out.println(accountsList.get(i).getPassword());
					if(accountsList.get(i).getPassword().equals(recievedPasswordStr)){
						foundThePassword = true ;
						System.out.println("found the password ! the iteration is: " + i);
					}

				}
			}
			else if(!foundTheMail){ // 3. if the account not found then send a message to the client
				// send a message to the client
				client.sendToClient("mail not found");
			}

			if(foundTheMail && !foundThePassword){ // 4. if the account found but password not found then send a message to the client
				// send a message to the client
				client.sendToClient("wrong password");
			}
		}


		if (msg instanceof ArrayList) { // arrived from the initializing of the program, so we initialize the database
			// with the starting Products
			System.out.println("Arrived here: msg instance of arrayList ");

			System.out.println("list size 11111 = "+ flowersnum);

			SessionFactory sessionFactory = getSessionFactory();
			session = sessionFactory.openSession();
			Transaction tx1 = session.beginTransaction();
			System.out.println("msg instance of arrayList ");


			List<Product> resultList = (List<Product>) msg;
			flowersnum = resultList.size();
			System.out.println("list size 2222 = "+ flowersnum);
			for (int i = 0; i < resultList.size(); i++) {
				//productGeneralList.set(i,resultList.get(i));
				session.save(resultList.get(i)); // save the Product in the database
				session.flush();
				System.out.println(resultList.get(i).getName());
			}
			tx1.commit();


			for (int i = 0; i < resultList.size(); i++) {
				productGeneralList.add(resultList.get(i));
				System.out.println(resultList.get(i).getName());
			}


			session.close();

		} else { // if we arrived to the else it means we reached here from the event handler of the "Apply Changes" button so what we do is take the changes on the product and update the database


		}

	}



	private static List<Product> getAllProducts() {
		System.out.println("Arrived to getAllProducts 1");
		CriteriaBuilder builder = session.getCriteriaBuilder();
		System.out.println("Arrived to getAllProducts 2");
		CriteriaQuery<Product> query = builder.createQuery(Product.class);
		System.out.println("Arrived to getAllProducts 3");
		query.from(Product.class);
		System.out.println("Arrived to getAllProducts 4");
		List<Product> result = session.createQuery(query).getResultList();
		System.out.println("Arrived to getAllProducts 5");
		return result;
	}

	Long countRows() {
		System.out.println("Arrived to coutnrwos 1");
		final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		System.out.println("Arrived to coutnrwos 2");
		CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
		System.out.println("Arrived to coutnrwos 3");
		Root<Product> root = criteria.from(Product.class);
		System.out.println("Arrived to coutnrwos 4");
		criteria.select(criteriaBuilder.count(root));
		System.out.println("Arrived to coutnrwos 5");
		return session.createQuery(criteria).getSingleResult();
	}


	void addItemToCatalog(Product recievedProd) {
		System.out.println("inside additemTocatalog1");
		long numOfRows = countRows();
		int castedId = (int) numOfRows;
		int newProductId = castedId + 1;
		recievedProd.setID(newProductId);
		System.out.println("inside additemTocatalog2");
		String recievedProductName = recievedProd.getName();
		System.out.println("inside additemTocatalog3");
		String recievedProductDetails = recievedProd.getDetails();
		System.out.println("inside additemTocatalog4");
		String recievedProductButton = recievedProd.getButton();
		System.out.println("inside additemTocatalog5");
		String recievedProductPrice = recievedProd.getPrice();
		System.out.println("inside additemTocatalog6");
		String recievedProductImage = recievedProd.getImage();
		System.out.println("inside additemTocatalog7");

		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		System.out.println("inside additemTocatalog8");
		System.out.println("the new index is:" + newProductId);

		session.save(recievedProd);
		System.out.println("inside additemTocatalog9");
		session.flush();
		System.out.println("inside additemTocatalog10");
		tx.commit();
		System.out.println("inside additemTocatalog11");

		System.out.println("inside additemTocatalog12");

	}


	void editCatalogProduct(Product productEdit){
		System.out.println("Arrived to edit catalog product 1");
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		int recievedProductID = productEdit.getID();
		String recievedProductName = productEdit.getName();
		String recievedProductDetails = productEdit.getDetails();
		String recievedProductButton = productEdit.getButton();
		String recievedProductPrice = productEdit.getPrice();
		String recievedProductImage = productEdit.getImage();

		System.out.println("Arrived to edit catalog product 2");
		Product updateProd  = session.load(Product.class, recievedProductID);

		//System.out.println(updateProd.getButton());
		//Product updateProd = (Product) persistentInstance1 ;
		//updateProd.setID(16);
		updateProd.setButton(recievedProductButton);
		updateProd.setPrice(recievedProductPrice);
		updateProd.setName(recievedProductName);
		updateProd.setDetails(recievedProductDetails);
		updateProd.setImage(recievedProductImage);

		System.out.println("Arrived to edit catalog product 3");
		System.out.println(updateProd.getID());
		session.update(updateProd);
		System.out.println("Arrived to edit catalog product 4");
		tx.commit();
		System.out.println("Arrived to edit catalog product 5");

		/*try {
			Product updatedProduct = new Product(recievedProductID, recievedProductButton, recievedProductName, recievedProductDetails, recievedProductPrice);
			for (int i = 0; i < productGeneralList.size(); i++) {
				if (productGeneralList.get(i).getID() == recievedProductID) {
					productGeneralList.set(i, updatedProduct);
				}
			}
			*//* USE UPDATE METHOD IN THE FUTURE *//*
			Saveinsess();
			tx.commit();
		} catch (Exception exception) {
			if (session != null) {
				session.getTransaction().rollback();
			}
			System.err.println("An error occured, changes have been rolled back.");
			exception.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}*/
	}

	void removeItemFromCatalog(String prodIdToRemove, ConnectionToClient _client) {


		System.out.println("arrived to removeItemFromCatalog 1");

		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();


		flowersnum--;

		int removedId = Integer.parseInt(prodIdToRemove);
		productGeneralList = getAllProducts();
		productGeneralList.remove(removedId-1); // remove the wanted item from the list
		for(int i=0;i<productGeneralList.size();i++){ // update all the items id's
			if(productGeneralList.get(i).getID() > removedId){
				productGeneralList.get(i).setID((productGeneralList.get(i).getID()-1));
			}
		}
		System.out.println("arrived to removeItemFromCatalog 2");


		session = sessionFactory.openSession();
		Transaction tx1 = session.beginTransaction();
		long longID = countRows();
		session.close();
		//tx1.commit();
		System.out.println("arrived to removeItemFromCatalog 3 and the longID is " + longID);
		int castedID = (int) longID;
		for(int l=0;l<castedID;l++){

			System.out.println("arrived to removeItemFromCatalog 2.5");
			deleteProduct(l+1);
		}

		session = sessionFactory.openSession();
		Transaction tx2 = session.beginTransaction();
		for(int i=0;i<productGeneralList.size();i++){
			session.save(productGeneralList.get(i));
			session.flush();
		}
		tx2.commit();
		session.close();

		//session.close(); // here we finished deleting a product, everything else is for updating the id's
		System.out.println("arrived to removeItemFromCatalog 2.8");



	}




	public void deleteProduct(int deleteIndex) {
		System.out.println("arrived to deleteProd 1");
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		System.out.println("arrived to deleteProd 2");

		Object persistentInstance = session.load(Product.class, deleteIndex);
		Product perProd = (Product) persistentInstance;
		System.out.println("arrived to deleteProd 3");
		if (persistentInstance != null) {
			session.delete(perProd);
		}
		System.out.println("arrived to deleteProd 4");

		tx.commit();
		session.close();

	}

	public void deleteAllProducts(){
		/*SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();*/
		System.out.println("arrived to deleteAllProducts 1");
		long rows = countRows();
		System.out.println("arrived to deleteAllProducts 2");
		int castedRows = (int) rows ;
		System.out.println("arrived to deleteAllProducts 3");
		for(int i=1;i<=castedRows;i++){
			deleteProduct(i);
		}
		System.out.println("arrived to deleteAllProducts 4");
	}
	public static List<Account> getAllAccounts() {
		System.out.println("Arrived to getAllAccounts 1");
		CriteriaBuilder builder = session.getCriteriaBuilder();
		System.out.println("Arrived to getAllAccounts 2");
		CriteriaQuery<Account> query = builder.createQuery(Account.class);
		System.out.println("Arrived to getAllAccounts 3");
		query.from(Account.class);
		System.out.println("Arrived to getAllAccounts 4");
		List<Account> resultlest = session.createQuery(query).getResultList();
		System.out.println("Arrived to getAllAccounts 5");
		return resultlest;
	}

	public void addAccount(Account newAcc) {

		System.out.println("inside additemTocatalog1");
		long numOfRows = countAccountRows();
		int castedId = (int) numOfRows;
		int newId = castedId + 1;
		newAcc.setAccountID(newId);
		System.out.println("inside additemTocatalog2");
		String recievedName = newAcc.getFullName();
		System.out.println("inside additemTocatalog3");
		String Adress=newAcc.getAddress();
		System.out.println("inside additemTocatalog4");
		String Email=newAcc.getEmail();
		String Password=newAcc.getPassword();
		long Phonnum=newAcc.getPhoneNumber();
		long creditcardnum=newAcc.getCreditCardNumber();
		Date newdate=newAcc.getCreditCardExpire();
		int Cvv=newAcc.getCcv();
		boolean is_login=newAcc.getLogged();
		int belongedshop=newAcc.getBelongShop();
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		System.out.println("inside additemTocatalog8");
		System.out.println("the new index is:" + newId);

		session.save(newAcc);
		session.flush();
		tx.commit();
		System.out.println("khaled");
		session.close();
	}
	public void removeAccount(String AccIdToRemove, ConnectionToClient _client) {


		System.out.println("arrived to removeItemFromCatalog 1");

	/*	SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();*/


		//Acc_num--;

		int removedId = Integer.parseInt(AccIdToRemove);
		System.out.println("mr7ba");
		accountGeneralList = getAllAccounts();
		accountGeneralList.remove(removedId-1); // remove the wanted item from the list
		for(int i=0;i<accountGeneralList.size();i++){ // update all the items id's
			if(accountGeneralList.get(i).getAccountID()> removedId){
				accountGeneralList.get(i).setAccountID((accountGeneralList.get(i).getAccountID()-1));
			}
		}
		System.out.println("mr7ba2");


		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx1 = session.beginTransaction();
		System.out.println("mr7ba3");
		long longID = countAccountRows();
		tx1.commit();
		session.close();
		System.out.println("mr7ba4");
		System.out.println("arrived to removeItemFromCatalog 3 and the longID is " + longID);
		int castedID = (int) longID;
		System.out.println(castedID);
		for(int l=0;l<castedID;l++){

			System.out.println("arrived to removeItemFromCatalog 2.5");
			deleteAccount(l+1);
		}
		System.out.println("mr7ba5");
		sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx2 = session.beginTransaction();
		for(int i=0;i<accountGeneralList.size();i++){
			session.save(accountGeneralList.get(i));
			session.flush();
		}
		System.out.println("mr7ba6");
		tx2.commit();
		session.close();
		System.out.println("mr7ba7");
		session.close(); // here we finished deleting a product, everything else is for updating the id's
		System.out.println("arrived to removeItemFromCatalog 2.8");



	}
	public void deleteAccount(int deleteIndex) {
		System.out.println("arrived to deleteProd 1");
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		System.out.println("arrived to deleteProd 2");

		Object persistentInstance = session.load(Account.class, deleteIndex);
		Account peracc = (Account) persistentInstance;
		System.out.println("arrived to deleteProd 3");
		if (persistentInstance != null) {
			session.delete(peracc);
		}
		tx.commit();
		session.close();

	}
	public Long countAccountRows() {
		System.out.println("Arrived to coutnrwos 1");
		final CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		System.out.println("Arrived to coutnrwos 2");
		CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
		System.out.println("Arrived to coutnrwos 3");
		Root<Account> root = criteria.from(Account.class);
		System.out.println("Arrived to coutnrwos 4");
		criteria.select(criteriaBuilder.count(root));
		System.out.println("Arrived to coutnrwos 5");
		System.out.println(session.createQuery(criteria).getSingleResult());
		return session.createQuery(criteria).getSingleResult();
	}
	public void editAccount(Account accountEdit){
		System.out.println("Arrived to edit catalog product 1");
		SessionFactory sessionFactory = getSessionFactory();
		session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		int recievedAccountID = accountEdit.getAccountID();
		String recievedAccountName = accountEdit.getFullName();


		String Adress=accountEdit.getAddress();

		String Email=accountEdit.getEmail();
		String Password=accountEdit.getPassword();
		long Phonnum=accountEdit.getPhoneNumber();
		long creditcardnum=accountEdit.getCreditCardNumber();
		Date newdate=accountEdit.getCreditCardExpire();
		int Cvv=accountEdit.getCcv();
		boolean is_login=accountEdit.getLogged();
		int belongedshop=accountEdit.getBelongShop();

		System.out.println("Arrived to edit catalog product 2");
		Account updateAccount  = session.load(Account.class, recievedAccountID);


		updateAccount.setFullName(recievedAccountName);
		updateAccount.setAddress(Adress);
		updateAccount.setEmail(Email);
		updateAccount.setPassword(Password);
		updateAccount.setPhoneNumber(Phonnum);
		updateAccount.setCreditCardNumber(creditcardnum);
		updateAccount.setCcv(Cvv);
		updateAccount.setCreditCardExpire(newdate);
		updateAccount.setLogged(is_login);
		updateAccount.setBelongShop(belongedshop);
		session.update(updateAccount);
		tx.commit();
		session.close();


		/*try {
			Product updatedProduct = new Product(recievedProductID, recievedProductButton, recievedProductName, recievedProductDetails, recievedProductPrice);
			for (int i = 0; i < productGeneralList.size(); i++) {
				if (productGeneralList.get(i).getID() == recievedProductID) {
					productGeneralList.set(i, updatedProduct);
				}
			}
			*//* USE UPDATE METHOD IN THE FUTURE *//*
			Saveinsess();
			tx.commit();
		} catch (Exception exception) {
			if (session != null) {
				session.getTransaction().rollback();
			}
			System.err.println("An error occured, changes have been rolled back.");
			exception.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}*/
	}


}