/**
 * Your application code goes here<br>
 * This file was generated by <a href="https://www.codenameone.com/">Codename
 * One</a> for the purpose of building native mobile applications using Java.
 */
package userclasses;

import ch.bbbaden.m335.memories.MyApplication;
import com.codename1.capture.Capture;
import com.codename1.components.ImageViewer;
import com.codename1.components.MultiButton;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Storage;
import com.codename1.io.Util;
import com.codename1.l10n.ParseException;
import com.codename1.l10n.SimpleDateFormat;
import com.codename1.media.Media;
import com.codename1.media.MediaManager;
import generated.StateMachineBase;
import com.codename1.ui.*;
import com.codename1.ui.events.*;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.util.Resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Your name here
 */
public class StateMachine extends StateMachineBase {

    private Memories memories;
    private Notes notes;
    private ArrayList<Memory> arrMemories;
    private ArrayList<Memory> memoriesOnForm = new ArrayList<>();

    public StateMachine(String resFile) {
        super(resFile);
        // do not modify, write code in initVars and initialize class members there,
        // the constructor might be invoked too late due to race conditions that might occur
    }

    /**
     * this method should be used to initialize variables instead of the
     * constructor/class scope to avoid race conditions
     */
    protected void initVars(Resources res) {
        notes = new Notes();
        System.out.println("\\\\");
    }

    @Override
    protected void onMain_BtnLaunchCameraAction(Component c, ActionEvent event) {
        String i = Capture.capturePhoto();
        if (i != null) {
            Image img = null;
            try {
                img = Image.createImage(i);
                MyImage image = new MyImage();
                image.setImage(img);
                image.toString();
                memories.getTodaysMemory().addImage(image);
                putMemoryInForm(memories.getTodaysMemory());
            } catch (IOException ex) {
                System.out.println("ex = " + ex);
            }
            findImageViewer().setImage(img);
        }

    }

    @Override
    protected void onMain_BtnChooseImageAction(Component c, ActionEvent event) {
        Form current = MyApplication.getCurrent();
        if (FileChooser.isAvailable()) {
            FileChooser.showOpenDialog(".pdf,application/pdf,.gif,image/gif,.png,image/png,.jpg,image/jpg,.tif,image/tif,.jpeg,.bmp", e2 -> {
                if (e2 != null && e2.getSource() != null) {
                    String file = (String) e2.getSource();
                    try {
                        Image img = Image.createImage(file);
                        MyImage myImage = new MyImage();
                        myImage.setImage(img);
                        myImage.toString();
                        current.add(new Label(img));
                        memories.getTodaysMemory().addImage(myImage);
                        putMemoryInForm(memories.getTodaysMemory());
                        if (true) {
                            return;
                        }
                    } catch (Exception ex) {
                        Log.e(ex);
                    }
                }
            });
        }
    }

    @Override
    protected void onMain_BtnRecordAction(Component c, ActionEvent event) {
        FileSystemStorage fs = FileSystemStorage.getInstance();
        String recordingsDir = fs.getAppHomePath() + "recordings/";
        fs.mkdir(recordingsDir);
        try {
            for (String file : fs.listFiles(recordingsDir)) {
                MultiButton mb = new MultiButton(file.substring(file.lastIndexOf("/") + 1));
                mb.addActionListener((e) -> {
                    try {
                        Media m = MediaManager.createMedia(recordingsDir + file, false);
                        m.play();
                    } catch (IOException err) {
                        Log.e(err);
                    }
                });
                MyApplication.getCurrent().add(mb);
            }
            try {
                String file = Capture.captureAudio();
                if (file != null) {
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MMM-dd-kk-mm");
                    String fileName = sd.format(new Date());
                    String filePath = recordingsDir + fileName;
                    Util.copy(fs.openInputStream(file), fs.openOutputStream(filePath));
                    MultiButton mb = new MultiButton(fileName);
                    mb.addActionListener((e) -> {
                        try {
                            Media m = MediaManager.createMedia(filePath, false);
                            m.play();
                        } catch (IOException err) {
                            Log.e(err);
                        }
                    });
                    MyApplication.getCurrent().add(mb);
                    MyApplication.getCurrent().revalidate();
                }
            } catch (IOException err) {
                Log.e(err);
            }

        } catch (IOException err) {
            Log.e(err);
        }

    }

    @Override
    protected void onMain_BtnNewNoteAction(Component c, ActionEvent event) {
        showForm("New Note", null);
    }

    @Override
    protected void onMain_BtnLoadMemoriesAction(Component c, ActionEvent event) {
        loadMemories();
        arrMemories = memories.getMemories();
        for (Memory i : arrMemories) {
            try {
                putMemoryInForm(i);
            } catch (NullPointerException e) {
                System.out.println("error error error");
            }
        }
//        Container container = findConMemories();
//        for (Memory i : memories.getMemories()) {
//            for (Note x : i.getNotes()) {
//                Label title = new Label(x.getTitle());
//                Label text = new Label(x.getText());
//                container.add(title);
//                container.add(text);
//            }
//        }
    }

    @Override
    protected void onNewNote_BtnSaveNoteAction(Component c, ActionEvent event) {
        KeyValue value = new KeyValue();
        Note note = new Note();
        if (!findTxtTitle().getText().equals("")) {
            note.setTitle(findTxtTitle().getText());
        }
        if (!findTxtText().getText().equals("")) {
            note.setText(findTxtText().getText());
        }
        notes.addNote(note);
        value.save(notes);
        memories.getTodaysMemory().addNote(note);
        System.out.println(memories.getTodaysMemory());
        back();
    }

    private void save() {
        Storage.getInstance().writeObject("Saved Data", memories.toHashSet());
    }

    private void putMemoryInForm(Memory mem) {
            memoriesOnForm.add(mem);
            ArrayList<MyImage> img = mem.getImages();
            ArrayList<Note> notes = mem.getNotes();
            Container con = new Container(BoxLayout.y());
            con.setUIID(mem.getDate().toString());
            ArrayList<Component> containers = (ArrayList<Component>) findConMemories().getChildrenAsList(true);
            for(Component i : containers){
                if(i.getUIID().equals(con.getUIID())){
                    System.out.println("found double container");
                    findConMemories().removeComponent(i);
                }
            }
            TextField txtTitle = new TextField(mem.getTitle());
            con.add(txtTitle);
            if (!img.isEmpty()) {
                for (MyImage i : img) {
                    addImageToForm(i, con);
                }
            }
            if (!notes.isEmpty()) {
                for (Note i : notes) {
                    addNoteToForm(i, con);
                }
            }
            try {
                findConMemories().addComponent(con); //need to do this after loading
            } catch (Exception e) {
                System.out.println("e = " + e + "\nCurrent working form: " + Display.getInstance().getCurrent().getTitle());
            }
            save();
    }

    private void addImageToForm(MyImage i, Container con) {
        ImageViewer imgViewer = new ImageViewer();
        imgViewer.setImage(i.getImage());
        con.add(imgViewer);
    }

    private void addNoteToForm(Note i, Container con) {
        Label lblTitle = new Label();
        try {
            lblTitle.setText(i.getTitle());
        } catch (Exception e) {
            System.out.println("error in title = " + e);
        }
        Label lblText = new Label();
        try {
            lblText.setText(i.getText());
        } catch (Exception e) {
            System.out.println("error in text = " + e);
        }
        try {
            con.add(lblTitle);
            con.add(lblText);
        } catch (Exception e) {
            System.out.println("couldn't add title or text to container");
        }
    }

    private void loadMemories() {
        if (memories == null) {
            memories = new Memories();
            try {
                memories.setMemories((ArrayList<String>) Storage.getInstance().readObject("Saved Data"));
            } catch (ParseException ex) {
                System.out.println("ex = " + ex);
            } catch (IOException ex) {
                System.out.println("ex = " + ex);
            } catch (java.text.ParseException ex) {
                System.out.println("ex = " + ex);
            } catch (NullPointerException e) {
                System.out.println("lulerror = " + e);
                try {
                    memories.setMemories(new ArrayList<String>());
                } catch (ParseException ex) {
                } catch (IOException ex) {
                } catch (java.text.ParseException ex) {
                }
            }
        }
    }

    @Override
    protected void postMain(Form f) {
        System.out.println("post show");
        loadMemories();
//        arrMemories = memories.getMemories();
//        for (Memory i : arrMemories) {
//            try {
//                putMemoryInForm(i);
//            } catch (NullPointerException e) {
//                System.out.println("error error error");
//            }
//        }
    }
}
