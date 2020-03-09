package com.dwiariyanto.androidkotlinsimpledemo.core.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import com.quang.appchat.base.BaseActivity

/**
 * Created by Dwi_Ari on 10/14/17.
 */

abstract class BaseFragment : Fragment() {
    val baseActivity: BaseActivity by lazy { activity as BaseActivity }
//    val defaultDialog: DefaultDialog by lazy { DefaultDialog() }
//    val loadingDialog: LoadingDialog by lazy { LoadingDialog() }

    val setup: Setup by lazy { setup() }
    val setupForToolbar: SetupForToolbar by lazy { setupForToolbar() }
    private var load = false

    abstract fun setup(): Setup
    abstract fun setupForToolbar(): SetupForToolbar
    abstract fun init()

    protected open fun onLoad() {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? = inflater.inflate(setup.layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        setupForToolbar.titleUse =
            if (setupForToolbar.titleRes != 0) getString(setupForToolbar.titleRes) else setupForToolbar.title
        setToolbarTitle(setupForToolbar.titleUse)

        load = true
        init()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (setupForToolbar.skipConfig) return
        menu?.clear()
        if (setupForToolbar.menuRes != 0) {
            inflater!!.inflate(setupForToolbar.menuRes, menu)
            onMenuCreated(menu!!)
        }
    }

    protected open fun onMenuCreated(menu: Menu) {
    }

    override fun onResume() {
        super.onResume()

        if (load) {
            load = false
            onLoad()
        }
    }


    open fun onBackPressed(): Boolean = false
    open fun restoreFromBackPress() {
        setToolbarTitle(setupForToolbar.titleUse)
    }

    protected fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    protected fun setToolbarTitle(title: String) {
        if (setupForToolbar.skipConfig) return
        setupForToolbar.titleUse = title

        if (title.isEmpty()) {
            baseActivity.supportActionBar?.hide()
        } else {
            val homeAsUp = setupForToolbar.homeAsUpEnable

            baseActivity.apply {
                supportActionBar?.apply {
                    show()
                    setDisplayHomeAsUpEnabled(homeAsUp)
                    if (setupForToolbar.homeAsUpIcon != 0) setHomeAsUpIndicator(setupForToolbar.homeAsUpIcon)
                    else setHomeAsUpIndicator(null)
                }
                setToolbarTitle(title)
            }
        }
    }

    fun isFragmentActive(): Boolean {
        val fragment = fragmentManager?.fragments?.lastOrNull { it is BaseFragment } ?: false
        return fragment == this
    }

    data class Setup(
        val tag: String,
        val layoutId: Int
    )

    data class SetupForToolbar(
        val skipConfig: Boolean = false,
        val title: String = "",
        val titleRes: Int = 0,
        val homeAsUpEnable: Boolean = true,
        val homeAsUpIcon: Int = 0,
        val toolbar: Int = 0,
        var menuRes: Int = 0
    ) {
        internal var titleUse: String = ""
    }
}
