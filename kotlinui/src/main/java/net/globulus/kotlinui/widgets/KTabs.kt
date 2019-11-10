package net.globulus.kotlinui.widgets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.R

typealias TabTitles = Array<Int>
typealias TabRenderer<D> = KView<CoordinatorLayout>.(D) -> KView<*>

class KTabs<D>(
    context: Context,
    fm: FragmentManager,
    @StringRes titleResId: Int,
    tabTitles: TabTitles,
    data: List<D>,
    renderer: TabRenderer<D>
) : KView<CoordinatorLayout>(context) {

  val appBarLayout: AppBarLayout
  val tabs: TabLayout
  val viewPager: ViewPager

  override val view = (LayoutInflater.from(context).inflate(R.layout.layout_tabs, null) as CoordinatorLayout).apply {
    appBarLayout = findViewById(R.id.appBarLayout)
    tabs = findViewById(R.id.tabs)
    viewPager = findViewById(R.id.viewPager)
  }

//  private val appBarLayout = AppBarLayout(context).apply {
//    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
////    addView(TextView(context).apply {
////      layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
////      gravity = Gravity.CENTER
//////      minHeight =
////      text = context.getString(titleResId)
////      setPadding(context.resources.getDimensionPixelSize(R.dimen.appbar_padding))
////      setTextAppearance(android.R.style.TextAppearance_Material_Widget_Toolbar_Title)
////    })
//    view.addView(this)
//  }
//
//  val tabs = TabLayout(context).apply {
//    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//    appBarLayout.addView(this)
//  }
//
//  val viewPager = ViewPager(context).apply {
//    id = View.generateViewId()
//    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//    view.addView(this)
////    val params = view.layoutParams as CoordinatorLayout.LayoutParams
////    params.behavior = AppBarLayout.ScrollingViewBehavior(view.context, null)
//  }

  init {
    val sectionsPagerAdapter = SectionsPagerAdapter(this, fm, tabTitles, data, renderer)
    viewPager.adapter = sectionsPagerAdapter
    tabs.setupWithViewPager(viewPager)
  }

  private class SectionsPagerAdapter<D>(
      private val owner: KTabs<D>,
      fm: FragmentManager,
      private val tabTitles: TabTitles,
      private val data: List<D>,
      private val renderer: TabRenderer<D>
  ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
      return TabFragment(owner.renderer(data[position]))
    }

    override fun getPageTitle(position: Int): CharSequence? {
     return owner.context.getString(tabTitles[position])
    }

    override fun getCount(): Int {
      return tabTitles.size
    }
  }

  class TabFragment(private val kView: KView<*>) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
      return kView.view
    }
  }
}

fun <D> AppCompatActivity.tabs(
    @StringRes titleResId: Int,
    tabTitles: TabTitles,
    data:
    List<D>,
    renderer: TabRenderer<D>
): KTabs<D> {
  val tabs = KTabs(this, supportFragmentManager, titleResId, tabTitles, data, renderer)
  setContentView(tabs.view)
  return tabs
}
