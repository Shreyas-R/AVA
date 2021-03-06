package srlabs.com.ava;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import static com.bumptech.glide.request.RequestOptions.fitCenterTransform;

public class fragment_home_a extends Fragment {
    private TextView no_updates_indicator;

    public fragment_home_a() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

      return inflater.inflate(R.layout.fragment_recycler_layout, container, false);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
            no_updates_indicator =  view.findViewById(R.id.no_updates_indicator);

            final RecyclerView recyclerView =  view.findViewById(R.id.RecyclerView_generic);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        final DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.keepSynced(true);

            final FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();


        final Query fb_link;


                fb_link =  mFirebaseDatabaseReference.child("home/"+fbUser.getUid()+"/home_info");
                no_updates_indicator.setText(R.string.no_a);



            FirebaseRecyclerAdapter<home_info_getter, ViewHolder> firebaseAdapter = new FirebaseRecyclerAdapter<home_info_getter, ViewHolder>(
                    home_info_getter.class,
                    R.layout.home_info_container_layout,
                    ViewHolder.class,
                    fb_link) {

                @Override
                protected void populateViewHolder(final ViewHolder viewHolder, home_info_getter HIG, int position) {
                    no_updates_indicator.setVisibility(View.GONE);

                    viewHolder.name.setText(HIG.getName());

                    DatabaseReference mDatabase_s = mFirebaseDatabaseReference.child("home/"+fbUser.getUid()+"/ava/sensors/"+HIG.getName());
                    DatabaseReference mDatabase_c = mFirebaseDatabaseReference.child("home/"+fbUser.getUid()+"/ava/controls/"+HIG.getName());

                    mDatabase_s.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                ava_getter ag = childSnap.getValue(ava_getter.class);

                                if(ag!=null) {
                                    long diff = new Date().getTime() - new Date(ag.gettimestampLong()).getTime();
                                    String timestamp = "Sensors • Updated ";
                                    if (diff < 60000) {
                                        timestamp = timestamp+"few sec";
                                    } else if (diff < 3600000) {
                                        timestamp  = timestamp+Long.toString(diff / 60000) + " m";
                                    } else {
                                        timestamp  = timestamp+Long.toString(diff / 3600000) + " h";
                                    }

                                    timestamp = timestamp+" ago";

                                    viewHolder.timestamp1.setText(timestamp);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mDatabase_c.orderByChild("timestamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                ava_getter ag = childSnap.getValue(ava_getter.class);

                                if (ag != null) {
                                    long diff = new Date().getTime() - new Date(ag.gettimestampLong()).getTime();
                                    String timestamp = "Controls • Accessed ";
                                    if (diff < 60000) {
                                        timestamp = timestamp + "few sec";
                                    } else if (diff < 3600000) {
                                        timestamp = timestamp + Long.toString(diff / 60000) + " m";
                                    } else {
                                        timestamp = timestamp + Long.toString(diff / 3600000) + " h";
                                    }

                                    timestamp = timestamp + " ago";

                                    viewHolder.timestamp2.setText(timestamp);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    viewHolder.RecyclerView1.setLayoutManager(new StaggeredGridLayoutManager(5,StaggeredGridLayoutManager.VERTICAL));
                    viewHolder.RecyclerView2.setLayoutManager(new StaggeredGridLayoutManager(5,StaggeredGridLayoutManager.VERTICAL));


                    FirebaseRecyclerAdapter<ava_getter, viewHolder_s> firebaseAdapter_s = new FirebaseRecyclerAdapter<ava_getter, viewHolder_s>(
                            ava_getter.class,
                            R.layout.ava_container_layout,
                            viewHolder_s.class,
                            mDatabase_s) {

                        @Override
                        protected void populateViewHolder(viewHolder_s viewHolder, ava_getter AG, int position) {

                            String status = AG.getStatus(), type = AG.getType();

                            viewHolder.ava_btn.setText(status);

                            int drawable;

                            if (type.equals("light")) {
                               drawable = R.drawable.ic_light;
                            } else if (type.equals("motion")) {
                                if(status.contains("Ended"))  drawable = R.drawable.ic_idle;
                                else drawable = R.drawable.ic_motion;
                            }  else drawable = 0;

                            viewHolder.ava_btn.setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0);

                        }
                    };
                    viewHolder.RecyclerView1.setAdapter(firebaseAdapter_s);

                    FirebaseRecyclerAdapter<ava_getter, viewHolder_c> firebaseAdapter_c = new FirebaseRecyclerAdapter<ava_getter, viewHolder_c>(
                            ava_getter.class,
                            R.layout.ava_container_layout,
                            viewHolder_c.class,
                            mDatabase_c) {

                        @Override
                        protected void populateViewHolder(viewHolder_c viewHolder, ava_getter AG, int position) {
                            viewHolder.ref = getRef(position).toString().replace("https://project-ava-a5c57.firebaseio.com/", "");
                            viewHolder.name = AG.getName();


                            String status = AG.getStatus(), type = AG.getType();

                            viewHolder.ava_btn.setHint(type);
                            viewHolder.status = status;


                            int drawable;

                            if (type.equals("led")) {
                                if (status.equals("1")) drawable = R.drawable.ic_led_variant_off;
                                else drawable = R.drawable.ic_led_variant_on;
                            } else if (type.equals("alarm")) {
                                if (status.equals("1")) drawable = R.drawable.ic_alarm_off;
                                else drawable = R.drawable.ic_alarm_on;
                            } else if (type.equals("bulb_lamp")) {
                                if (status.equals("1")) drawable = R.drawable.ic_bulb_lamp_off;
                                else drawable = R.drawable.ic_bulb_lamp_on;
                            } else drawable = 0;

                            viewHolder.ava_btn.setCompoundDrawablesWithIntrinsicBounds(0, drawable, 0, 0);

                            String t="";
                            if (!status.equals("1")) {
                                long diff = new Date().getTime() - new Date(AG.gettimestampLong()).getTime();

                            if (diff < 60000) {
                                t = "now";
                            } else if (diff < 3600000) {
                                t = Long.toString(diff / 60000) + " m";
                            } else {
                                t = Long.toString(diff / 3600000) + " h";
                            }


                        }
                            t = AG.getName()+"\n"+t;
                            viewHolder.ava_btn.setText(t);
                        }
                    };
                    viewHolder.RecyclerView2.setAdapter(firebaseAdapter_c);


                }

            };
            recyclerView.setAdapter(firebaseAdapter);




    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,timestamp1,timestamp2;
        RecyclerView RecyclerView1,RecyclerView2;

        public ViewHolder(View v) {
            super(v);

            name =  v.findViewById(R.id.name);
            timestamp1 =  v.findViewById(R.id.timestamp1);
            timestamp2 =  v.findViewById(R.id.timestamp2);
            RecyclerView1 = v.findViewById(R.id.RecyclerView1);
            RecyclerView2 = v.findViewById(R.id.RecyclerView2);


        }

    }
    public static class viewHolder_s extends RecyclerView.ViewHolder {
        Button ava_btn;

        public viewHolder_s(View v) {
            super(v);

            ava_btn =  v.findViewById(R.id.ava_btn);

        }
    }
    public static class viewHolder_c extends RecyclerView.ViewHolder {
        Button ava_btn;
        String ref,name,status;

        public viewHolder_c(View v) {
            super(v);

            ava_btn =  v.findViewById(R.id.ava_btn);
            ava_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(status.equals("1")) status="2";
                    else status="1";

                   final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(ref);

                    ava_getter ag = new ava_getter(name,status,0L,ava_btn.getHint().toString());
                    mDatabase.setValue(ag);

                //    mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
               //         @Override
               //         public void onComplete(@NonNull Task<Void> task) {
               //             mDatabase.child("timestamp").setValue(ServerValue.TIMESTAMP);
               //         }
              //      });

                }
            });
        }
    }
}
