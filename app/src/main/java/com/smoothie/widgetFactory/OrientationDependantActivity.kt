package com.smoothie.widgetFactory

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.smoothie.wirelessDebuggingSwitch.R

abstract class OrientationDependantActivity : AppCompatActivity {

    private val animationIn: Int
    private val animationOut: Int

    private lateinit var currentFragment: Fragment

    constructor(animationIn: Int, animationOut: Int) : super() {
        this.animationIn = animationIn
        this.animationOut = animationOut
    }

    constructor() : this(android.R.anim.slide_in_left, android.R.anim.slide_out_right)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_dependant)
        setAppropriateFragment(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setAppropriateFragment()
    }

    private fun setAppropriateFragment(replace: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.setCustomAnimations(animationIn, animationOut)

        if (replace) {
            currentFragment = createAppropriateFragment()
            transaction.replace(R.id.root_view, currentFragment)
        }
        else {
            transaction.remove(currentFragment)
            currentFragment = createAppropriateFragment()
            transaction.add(currentFragment, "New Fragment")
        }

        transaction.commit()
    }

    private fun createAppropriateFragment(): Fragment {
        return if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            createPortraitFragment()
        else
            createLandscapeFragment()
    }

    abstract fun createLandscapeFragment(): Fragment

    abstract fun createPortraitFragment(): Fragment

}
