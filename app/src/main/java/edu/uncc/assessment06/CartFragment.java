package edu.uncc.assessment06;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

import edu.uncc.assessment06.databinding.CartRowItemBinding;
import edu.uncc.assessment06.databinding.FragmentCartBinding;
import edu.uncc.assessment06.databinding.ProductRowItemBinding;
import okhttp3.OkHttpClient;

public class CartFragment extends Fragment
{
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db;
    private OkHttpClient client;
    public static final String TAG = "evaluation6";
    private CartAdapter adapter;

    ArrayList<Product> mProducts = new ArrayList<>();

    public CartFragment()
    {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    FragmentCartBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("My Cart");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setHasFixedSize(true);

        adapter = new CartAdapter();
        binding.recyclerView.setAdapter(adapter);
        getProducts();

    }

    class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder>
    {
        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            CartRowItemBinding binding = CartRowItemBinding.inflate(getLayoutInflater(), parent, false);
            return new CartViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position)
        {
            Product product = mProducts.get(position);
            holder.setupUI(product);
        }

        @Override
        public int getItemCount()
        {
            return mProducts.size();
        }

        class CartViewHolder extends RecyclerView.ViewHolder
        {
            CartRowItemBinding mBinding;
            Product mProduct;

            public CartViewHolder(CartRowItemBinding binding)
            {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Product product)
            {
                mProduct = product;

                mBinding.textViewProductName.setText(mProduct.getName());
                mBinding.textViewProductPrice.setText(mProduct.getPrice());
                Picasso.get().load(product.getImg_url()).into(mBinding.imageViewProductIcon);
//                mBinding.textViewCreatedAt.setText(DateFormat.format("mm/dd/yyyy",post.getCreatedAt().toDate()));
//                if (mProduct.getOwnerID().equals(mAuth.getCurrentUser().getUid()))
//                {
//                    mBinding.imageViewDeleteFromCart.setVisibility(View.VISIBLE);
//                }
//                else
//                {
//                    mBinding.imageViewDeleteFromCart.setVisibility(View.INVISIBLE);
//                }
                mBinding.imageViewDeleteFromCart.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Log.d(TAG, "onClick: product doc: " + mProduct.getDocRef());
                        db.collection("products").document(mProduct.getDocRef()).delete().addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Log.d(TAG, "onComplete: success delete product");
                                    Log.d(TAG, "onComplete: task: ");
                                    getProducts();
                                }
                                else
                                {
                                    Log.d(TAG, "onComplete: fail delete product " + task.getException().getMessage());
                                }
                            }
                        });
                    }
                });
            }

        }

    }

    private void getProducts()
    {

        db.collection("products").addSnapshotListener(new EventListener<QuerySnapshot>()
                {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error)
                    {
                        mProducts.clear();
                        for (QueryDocumentSnapshot doc : value)
                        {
                            mProducts.add(new Product(doc.getString("name"),
                                    doc.getString("img_url"),
                                    doc.getString("price"),
                                    doc.getString("pid"),
                                    doc.getString("docRef"),
                                    doc.getString("ownerID")));
                        }
                        adapter.notifyDataSetChanged();
                        String totalPrice = calcTotalPrice();
                        binding.textViewTotal.setText(totalPrice);
                    }
                });
//        db.collection("products").get().addOnCompleteListener(getActivity(), new OnCompleteListener<QuerySnapshot>()
//        {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task)
//            {
//                if (task.isSuccessful())
//                {
//                    mProducts.clear();
//                    for (DocumentSnapshot doc : task.getResult())
//                    {
//                        Product post = doc.toObject(Product.class);
//                        Log.d(TAG, "onComplete: get products success");
//                        mProducts.add(post);
//                        Log.d(TAG, "Product: " + post);
//                    }
//                    getActivity().runOnUiThread(new Runnable()
//                    {
//                        @Override
//                        public void run()
//                        {
//                            adapter.notifyDataSetChanged();
//                        }
//                    });
//
//                }
//            }
//        });
    }
    private String calcTotalPrice()
    {
        String defaultAmount = "$ 0.00";
        if (mProducts.isEmpty())
        {
            return defaultAmount;
        }
        double amountTotal = 0;
        for (Product product : mProducts)
        {
            amountTotal += Double.parseDouble(product.getPrice());
        }
        defaultAmount = "$ " + amountTotal;
        return defaultAmount;
    }
}