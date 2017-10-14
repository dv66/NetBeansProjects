package data.link.layer.assignment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author User
 */
public class profileController implements Initializable {

    @FXML
    Label receiveLabel;
    @FXML
    TextArea myConsole;
    @FXML
    TextField receiverText;
    @FXML
    TextField fileText;
    @FXML
    Button sendfileButton;
 
    @FXML
    Button logoutButton;
    
    @FXML
    Tab myFilesTab;
    @FXML
    TabPane tabs;

    @FXML
    TableView<TableRows> pendingTableView;
    @FXML
    TableColumn fileid;
    @FXML
    TableColumn filename;
    @FXML
    TableColumn filesize;
    @FXML
    TableColumn sender;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        fileid.setCellValueFactory(new PropertyValueFactory("fileId"));
        filename.setCellValueFactory(new PropertyValueFactory("fileName"));
        filesize.setCellValueFactory(new PropertyValueFactory("fileSize"));
        sender.setCellValueFactory(new PropertyValueFactory("sender"));
        pendingTableView.setItems(getRow());

        
        
        tabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if(newValue.getText().equals("My Files")){
                    try {
                        MessagingTools.rout.writeUTF(MessagingTools.username);
                        String msg = MessagingTools.rin.readUTF();
                        while(!msg.equals("done")){
                            System.out.println(msg);
                            msg = MessagingTools.rin.readUTF();
                        }
                        
                    } catch (Exception e) {}
                }
            }
        });
        
    }    
    
    


    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        
        if(event.getSource() == logoutButton){
            
            System.out.println("logged out");
            Stage stage = (Stage)logoutButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
            
            MessagingTools.dout.writeUTF("logout");
            MessagingTools.din.close();
            MessagingTools.dout.close();
            MessagingTools.socket.close();
            MessagingTools.rout.writeUTF("logout");
            MessagingTools.rin.close();
            MessagingTools.rout.close();
            MessagingTools.rSocket.close();

        }
        else if(event.getSource() == sendfileButton){
            
            MessagingTools.dout.writeUTF("sendfile");
            
            String receiver = receiverText.getText();
            String fileLocation = fileText.getText();
            
            MessagingTools.dout.writeUTF(receiver);
            String confirm = MessagingTools.din.readUTF();
            
            if(confirm.equals("no")){
                receiveLabel.setText("receiver is not online try later");
            }else{
                receiveLabel.setText("");
                System.out.println(fileLocation);
                File senderFile = new File(fileLocation);
                System.out.println(senderFile.getName());
                byte[] fileToByteArray = Files.readAllBytes(Paths.get(fileLocation));
                MessagingTools.dout.writeUTF(senderFile.getName());                     // SENDING FILE NAME
                MessagingTools.dout.writeUTF(String.valueOf(fileToByteArray.length));   // SENDING FILE SIZE


                String confirmationMessage = MessagingTools.din.readUTF();
                
                if(confirmationMessage.equals("SERVER LOADED")){
                    myConsole.appendText(confirmationMessage + "\n");
                            
                }
                else{
                    int fileId = Integer.parseInt(MessagingTools.din.readUTF());
                    int chunkSize = Integer.parseInt(MessagingTools.din.readUTF());
                    int excess = Integer.parseInt(MessagingTools.din.readUTF());
                    int totalChunks = Integer.parseInt(MessagingTools.din.readUTF());

                    

                    int i = 0, j, chunkNumber = 1, total = 0;
                    OutputStream outputStream = MessagingTools.socket.getOutputStream();
                    
                    while (i < fileToByteArray.length) {

                        if (i + chunkSize >= fileToByteArray.length)
                            j = fileToByteArray.length;
                        else j = i + chunkSize;


                        MessagingTools.dout.writeUTF(String.valueOf(j - i));
                        byte[] bytes = Arrays.copyOfRange(fileToByteArray, i, j);
                        outputStream.write(bytes);
                        outputStream.flush();

                        long startTime = System.currentTimeMillis();
                        myConsole.appendText(MessagingTools.din.readUTF());
                        long endTime = System.currentTimeMillis();

                        String isTimeout = "no";

                        if(endTime - startTime > 30 * 1000){
                            isTimeout = "yes";
                            MessagingTools.dout.writeUTF(isTimeout);
                            break;
                        }
                        MessagingTools.dout.writeUTF(isTimeout);

                        i = j;
                        chunkNumber++;
                    }

                    myConsole.appendText(MessagingTools.din.readUTF() + "\n");
                }
            }
        }
    }
    
    
    
    
    public  ObservableList<TableRows> getRow( ){
            ObservableList<TableRows> tableRows = FXCollections.observableArrayList();
            tableRows.add(new TableRows("ab","ab","ab","ab" ));
            tableRows.add(new TableRows("ab","ab","ab","ab" ));
            tableRows.add(new TableRows("ab","ab","ab","ab" ));
            tableRows.add(new TableRows("ab","ab","ab","ab" ));
            return tableRows;
    }
    
}


