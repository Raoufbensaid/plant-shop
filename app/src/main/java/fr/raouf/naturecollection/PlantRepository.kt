package fr.raouf.naturecollection

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import fr.raouf.naturecollection.PlantRepository.Singleton.databaseRef
import fr.raouf.naturecollection.PlantRepository.Singleton.plantList
import javax.security.auth.callback.Callback

class PlantRepository {

    object Singleton {

        // se connecter à la reference "plants"
        val databaseRef = FirebaseDatabase.getInstance().getReference("plants")

        //créer une liste qui va contenir nos plantes
        val plantList = arrayListOf<PlantModel>()
    }

    fun updateData(callback: () -> Unit) {
        // absorber les données depuis la databaseRef -> liste de plantes
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // retirer les anciennes
                plantList.clear()

                //recolter la liste
                for (ds in snapshot.children) {
                    // construire un objet plante
                    val plants = ds.getValue(PlantModel::class.java)

                    // verifier que la plante n'est pas null
                    if (plants != null) {
                        //ajouter la plante à notre liste
                        plantList.add(plants)
                    }
                }

                // actionner le callback
                callback()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // mettre à jour un objet plante en bdd
    fun updatePlant(plant: PlantModel) = databaseRef.child(plant.id).setValue(plant)
}