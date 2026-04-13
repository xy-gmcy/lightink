package cn.lightink.reader.ui.discover.storage

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cn.lightink.reader.R
import cn.lightink.reader.ui.base.LifecycleActivity
import kotlinx.android.synthetic.main.activity_storage.*

class StorageActivity : LifecycleActivity() {

    companion object {
        const val PAGE_BOOK = 0
        const val PAGE_BOOK_SOURCE = 1
        const val EXTRA_PAGE = "extra_page"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        val defaultPage = intent.getIntExtra(EXTRA_PAGE, PAGE_BOOK)

        mViewPager.adapter = StoragePagerAdapter(supportFragmentManager)

        mViewPager.offscreenPageLimit = 2
        mViewPager.currentItem = defaultPage
    }

    override fun onBackPressed() {
        val fragment =
            supportFragmentManager.findFragmentByTag(
                "android:switcher:${R.id.mViewPager}:${mViewPager.currentItem}"
            )

        if (fragment is StorageFragment && fragment.onBackPressed()) {
            return
        }

        super.onBackPressed()
    }

    class StoragePagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                PAGE_BOOK_SOURCE -> BookSourceImportFragment()
                else -> BookImportFragment()
            }
        }
    }
}