package org.personal.videotogether.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_sign_in.*
import org.personal.videotogether.R

class SignInFragment : Fragment(), View.OnClickListener {

    private val TAG = javaClass.name

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        setListener()
    }

    private fun setListener() {
        signInBtn.setOnClickListener(this)
        signUpTV.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when(view?.id) {

            R.id.signInBtn -> {
                Log.i(TAG, "onClick: 로그인 구현하기")
            }

            R.id.signUpTV -> {
                navController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }
}