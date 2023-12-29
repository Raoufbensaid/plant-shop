package fr.raouf.naturecollection

import android.net.Uri
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import fr.raouf.naturecollection.PlantRepository.Singleton.databaseRef
import fr.raouf.naturecollection.PlantRepository.Singleton.downloadUri
import fr.raouf.naturecollection.PlantRepository.Singleton.plantList
import fr.raouf.naturecollection.PlantRepository.Singleton.storageReference
import java.util.UUID

class PlantRepository {

    object Singleton {
        //donner le lien pour acceder au bucket
        private val BUCKET_URL: String = "gs://naturecollection-3413f.appspot.com"

        // se connecter à notre espace de stockage
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(BUCKET_URL)

        // se connecter à la reference "plants"
        val databaseRef = FirebaseDatabase.getInstance().getReference("plants")

        //créer une liste qui va contenir nos plantes
        val plantList = arrayListOf<PlantModel>()

        // contenir le lien de l'image courante
        var downloadUri: Uri? = null
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

    // créer une fonction pour envoyer des fichiers sur le storage
    fun uploadImage(file: Uri, callback: () -> Unit) {
        // verifier que le fichier n'est pas null
        if (file != null) {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val ref = storageReference.child(fileName)
            val uploadTask = ref.putFile(file)

            // demarrer la tache d'envoi
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                // s'il y a eu un probleme lors de l'envoi du fichier
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                return@Continuation ref.downloadUrl

            }).addOnCompleteListener { task ->
                //verifier si tout a bien fonctionné
                if (task.isSuccessful) {
                    // reccuperer l'image
                    downloadUri = task.result
                    callback()
                }
            }
        }
    }

    // mettre à jour un objet plante en bdd
    fun updatePlant(plant: PlantModel) = databaseRef.child(plant.id).setValue(plant)

    // inserer une nouvelle plante en bdd
    fun insertPlant(plant: PlantModel) = databaseRef.child(plant.id).setValue(plant)

    //supprimer une plante de la bdd
    fun deletePlant(plant: PlantModel) = databaseRef.child(plant.id).removeValue()
}