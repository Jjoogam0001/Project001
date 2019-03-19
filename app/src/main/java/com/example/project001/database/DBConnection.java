package com.example.project001.database;

import android.support.annotation.NonNull;
import android.util.Log;
import com.example.project001.RidersActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static com.google.android.gms.wearable.DataMap.TAG;

public class DBConnection {

    //Variables
    Trip trip;




    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ArrayList<Trip> trips = new ArrayList<>();

    public RidersActivity r;


    //method for registering the user
    private void addUserToDB(String email, String name) {

        // Create a new user with a first and last name
        Map<String, Object> person = new HashMap<>();

        person.put("name", name);
        person.put("email", email);

        // Add a new document with a generated ID
        db.collection("person")
                .add(person)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


    //method for checking if the email exists
    public void checkIfExists(final String email, final String name) {

        db.collection("person")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                //if exists break
                                if(document.get("email").equals(email)) {
                                    Log.e("the email", "exists");
                                    return;
                                }
                            }
                            //if doesn't exist
                            addUserToDB(email, name);

                        } else {
                            Log.e("data", "inserting");
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    //method to add a trip
    public void addTripToDB(Object trip) {
        db.collection("trip").document().set(trip);
    }

    public void getTripsforMap() {

        trips.clear();

        db.collection("trip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        trip = new Trip(
                                document.get("date").toString(),
                                document.get("time").toString(),
                                document.get("departure").toString(),
                                document.get("destination").toString(),
                                document.get("price").toString(),
                                document.get("seats").toString(),
                                document.getString("author"));


                        trips.add(trip);
                        System.out.println("SUZ ARRAY: " + trips.size());

                    }
                    r.getArrayList(trips);

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    //tripId, passenger, driver, soolean
    //0 = pending
    //1 = accepted
    //2 = declined
    public void addTripRequest(final String driver, final String passenger, final String status,
                               final String departure, final String destination, final String date){

        db.collection("trip").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                   System.out.println("THE VALUES: "
                            + "\ndriver: "+ driver
                            + "\npassenger: " + passenger
                            + "\ndeparture: " + departure
                            + "\ndestination: " + destination
                            + "\ndate: " + date
                            + "\nstatus: " + status);


                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String tripId;

                       System.out.println("THE VALUES DB: "
                               + "\ndriver: "+ document.getString("author")
                                + "\ndeparture: "+ document.getString("departure")
                               + "\ndestination: "+ document.getString("destination")
                               + "\ndate: "+ document.getString("date"));

                        if(document.getString("author").equals(driver)){
                            Log.e("", "You can't request a trip where you are the driver.");
                            break;

                        }else if(document.getString("author").equals(driver) &&
                                document.getString("destination").equals(destination) &&
                                document.getString("departure").equals(departure) &&
                                document.getString("date").equals(date)){
                            tripId = document.getId();
                            Request request = new Request(tripId, passenger, driver, status);
                            db.collection("request").document().set(request);
                            Log.e("", "Added to request table.");
                            break;

                        }else{
                            Log.d(TAG, "Can't find the trip in the database: ", task.getException());

                        }


                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }




}
