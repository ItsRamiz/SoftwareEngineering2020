package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;

public class UpdateMessage implements Serializable {

    private String updateClass;
    private String updateFunction ;
    private String deleteId;
    private int id;
    private Product product = null;
    private Account account = null;
    private Worker worker = null;
    private Manager manager = null;


    public UpdateMessage(String update_class,String update_function){
        this.updateClass = update_class;
        this.updateFunction = update_function;

    }

    public String getUpdateClass(){
        return this.updateClass;
    }
    public String getUpdateFunction(){
        return this.updateFunction;
    }

    public String getDelteId(){
        return this.deleteId;
    }

    public int getId(){
        return this.id;
    }

    public Product getProduct(){
        return this.product;
    }

    public Account getAccount(){
        return this.account;
    }

    public void setProduct(Product prod){
        this.product = prod;
    }

    public void setAccount(Account acc){
        this.account = acc;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setDelteId(String delete_id){
        this.deleteId = delete_id;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Worker getWorker() {
        return worker;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }
}

