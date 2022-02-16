package com.example.fotografpaylasmauygulamasi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_haberler.*

class HaberlerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //ekleme
    private lateinit var database:FirebaseFirestore
    var postListesi =ArrayList<ModelClass>() //6.6)Bunu olusturdulk
    private lateinit var recyclerViewAdapter : HarberRecyclerAdapter //7.0)adaoter kullanmka için oluışturuldu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haberler)
        auth= FirebaseAuth.getInstance()
        database= FirebaseFirestore.getInstance()
        veriAl()
        //7.1)Burda Recycler sınıfına verş göndermek almka için yazdık
        var layoutManager=LinearLayoutManager(this)
        recyclerView.layoutManager=layoutManager
        recyclerViewAdapter= HarberRecyclerAdapter(postListesi)
        recyclerView.adapter=recyclerViewAdapter
        //recyclerViewAdapter.notifyDataSetChanged() For loopun hemen altına!!!!//7.2)Bunu unutma!!! Çünkü for loopdan sonra yeni veri geldiğinde kendini yenile dedik

    }
    fun veriAl()
    {
       //database.collection("Post").document("").get() //6)koleksiyonu 1 defaya mahsus çekebiliriz ama bu bize lazım degil
        database.collection("Post").orderBy("Tarih",Query.Direction.DESCENDING).addSnapshotListener { snapshot, exception ->

            if(exception !=null)
            {//6.3hata varsa
                Toast.makeText(this,exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
            else
            {
                if(snapshot !=null) //6.4)Snapshotda bos geliyor kkonmtrol saglamam lazım
                {
                    if(!snapshot.isEmpty) //6.5)snapshot bos olmayabilir amaiçinde de bişi olmaması durumu kontrol ediliyor burda ! ile null değil diyebiliriz yada snapshot.isEmty==false deriz
                    {
                        val documents=snapshot.documents
                        postListesi.clear() //6.9)Önceden içeride kalan birşey varsa silinsin diye !!
                        for (i in documents)
                        {
                            val email=i.get("KullaniciAdi") as String //Casting yapıyoruz cünkü verilerin içi boş ne oldugunu bilmiyor. println(email) diyerek çalışıyormu diye baktık çalıştı
                            val kullaniciyorum=i.get("Yorum") as String
                            val kullanicigorsel=i.get("GorselinUrlsi") as String
                            //6.7)BUNU OLUSTURDUK ÇÜNKÜ 10 FARKLI DEĞİŞKEN OLSAYDI TEK TEK EKLEMEK YERİNE CLASS ÜZERİNDEN TEK PARÇADA ALIYORUZ.OOP İÇİN ÖNEMLİ !!
                            val indirilenPost=ModelClass(email,kullaniciyorum,kullanicigorsel)
                            postListesi.add(indirilenPost)//6.8)Listeye ekledik
                        }
                        recyclerViewAdapter.notifyDataSetChanged() //7.2)Bunu unutma!!! Çünkü for loopdan sonra yeni veri geldiğinde kendini yenile dedik
                    }
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //3)Menüyü bu sınıfa bağlıyoruz.artık menü buraya bağlı
        val menuInflater=menuInflater
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //4)Menü seçilince ne olacağını yazıcaz
        if (item.itemId==R.id.fotografPaylas)
        {//4.1)Fotoğraf paylaşma bölümü açılacaktır.
            val intent=Intent(this,FotografPaylasmaActivty::class.java)
            startActivity(intent) //4.5)finish yapmıyorum çünkü kullanıcı iptal etmek isteyebilir geriye çıakbilir.
        }
        else if(item.itemId==R.id.cikisYap)
        {//4.2)Cikis islemi ypaılacak burada
            auth.signOut() //4.4)Cikis islemini yaptık
            val intent= Intent(this,KullaniciActivity::class.java)
            startActivity(intent)
            finish() //4.3)Adam cıkıs yaptıktığı için kullanıcı uygulamayı açınca girmesin.Bide firebaseden cıkıs yapmam lazım.KullaniciActivty icindeki guncelKullanıcı null deger dönmeli

        }
        return super.onOptionsItemSelected(item)
    }
}