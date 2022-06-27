package il.cshaifasweng.OCSFMediatorExample.client;

import com.sun.javafx.image.IntPixelGetter;
import il.cshaifasweng.OCSFMediatorExample.entities.Account;
import il.cshaifasweng.OCSFMediatorExample.entities.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.net.MalformedURLException;

public class CheckoutController {

    @FXML
    private CheckBox anotherMethodBox;

    @FXML
    private TextField creditNumberField;

    @FXML
    private Text creditNumberText;

    @FXML
    private TextField cvvField;

    @FXML
    private Text cvvText;

    @FXML
    private ComboBox<Integer> dayCheckout;

    @FXML
    private CheckBox deliveryBox;

    @FXML
    private Text deliveryDateCheckout;

    @FXML
    private ComboBox<Integer> expiryMonth;

    @FXML
    private Text expiryText;

    @FXML
    private ComboBox<Integer> expiryYear;

    @FXML
    private ComboBox<String> hourCheckout;

    @FXML
    private CheckBox greetingBoxCheckout;

    @FXML
    private TextField greetingTextCheckout;

    @FXML
    private ComboBox<Integer> monthCheckout;

    @FXML
    private Button placeOrderButton;

    @FXML
    private TextField recepAddressField;

    @FXML
    private Text recepAddressText;

    @FXML
    private TextField recepNameField;

    @FXML
    private Text recepNameText;

    @FXML
    private TextField recepPhoneField;

    @FXML
    private Text recepPhoneText;

    @FXML
    private ComboBox<Integer> yearCheckout;

    @FXML
    private Button back;

    @FXML
    private CheckBox deliverToHome;


    @FXML
    void openCatalog(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent roott = loader.load();
        PrimaryController cc = loader.getController();
        Stage stage = new Stage();
        stage.setScene(new Scene(roott));
        stage.setTitle("Catalog");
        stage.show();
        Stage stagee = (Stage)back.getScene().getWindow();
        // do what you have to do
        stagee.close();

    }
    @FXML
    void PlaceOrder(ActionEvent event)
    {
        String chainShop = chooseShopID.getSelectionModel().toString();
    }

    @FXML
    void DeliverToMyHome(ActionEvent event)
    {
        deliveryBox.setVisible(true);
    }

    @FXML
    void addGreeting(ActionEvent event) {

        if(greetingBoxCheckout.isSelected())
            greetingTextCheckout.setVisible(true);
        else
            greetingTextCheckout.setVisible(false);
    }

    @FXML
    void deliverySomeone(ActionEvent event) {

        boolean mode;
        if(deliveryBox.isSelected())
            mode = true;
        else
            mode = false;
        recepNameText.setVisible(mode);
        recepNameField.setVisible(mode);
        recepPhoneText.setVisible(mode);
        recepPhoneField.setVisible(mode);
        recepAddressText.setVisible(mode);
        recepAddressField.setVisible(mode);
    }

    @FXML
    void useAnotherMethod(ActionEvent event)
    {
        boolean mode;
        if(anotherMethodBox.isSelected())
            mode = true;
        else
            mode = false;
        creditNumberField.setVisible(mode);
        creditNumberText.setVisible(mode);
        expiryText.setVisible(mode);
        expiryYear.setVisible(mode);
        expiryMonth.setVisible(mode);
        cvvText.setVisible(mode);
        cvvField.setVisible(mode);
    }


    @FXML
    private ComboBox<String> chooseShopID;

    @Subscribe
    public void PassAccountEvent(PassAccountEventCheckout passAcc){ // added today
        System.out.println("Arrived To Pass Account - CheckoutController");
        Account recvAccount = passAcc.getRecievedAccount();
        System.out.println(recvAccount.getPassword());
        System.out.println(recvAccount.getAccountID());
        System.out.println(recvAccount.getEmail());
        System.out.println(recvAccount.getFullName());
        System.out.println(recvAccount.getAddress());
        System.out.println(recvAccount.getCreditCardNumber());
        System.out.println(recvAccount.getCreditMonthExpire());
    }
    @FXML
    void initialize() throws MalformedURLException
    {
        EventBus.getDefault().register(this);
        int i;
        for(i = 1 ; i < 31 ; i++)
        {
            dayCheckout.getItems().add(i);
        }
        for(i = 1 ; i < 13 ; i++)
        {
            expiryMonth.getItems().add(i);
            monthCheckout.getItems().add(i);
        }
        for(i = 2022 ; i < 2030 ; i++)
        {
            yearCheckout.getItems().add(i);
            expiryYear.getItems().add(i);
        }
        int startHour = 7;
        String FullHour;
        for(i = 0 ; i < 15 ; i++)
        {
            FullHour = startHour + ":" + "00";
            FullHour = startHour + ":" + "15";
            FullHour = startHour + ":" + "30";
            FullHour = startHour + ":" + "45";
            hourCheckout.getItems().add(FullHour);
            startHour++;
        }
        chooseShopID.getItems().add("ID 1: Tiberias, Big Danilof");
        chooseShopID.getItems().add("ID 2: Haifa, Merkaz Zeiv");
        chooseShopID.getItems().add("ID 3: Tel Aviv, Ramat Aviv");
        chooseShopID.getItems().add("ID 4: Eilat, Ice mall");
        chooseShopID.getItems().add("ID 5: Be'er Sheva, Big Beer Sheva");
        recepNameText.setVisible(false);
        recepNameField.setVisible(false);
        recepPhoneText.setVisible(false);
        recepPhoneField.setVisible(false);
        recepAddressText.setVisible(false);
        recepAddressField.setVisible(false);

        creditNumberField.setVisible(false);
        creditNumberText.setVisible(false);
        expiryText.setVisible(false);
        expiryYear.setVisible(false);
        expiryMonth.setVisible(false);

        cvvField.setVisible(false);
        cvvText.setVisible(false);

        greetingTextCheckout.setVisible(false);
        deliveryBox.setVisible(false);
    }
}