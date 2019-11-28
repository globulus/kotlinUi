package net.globulus.kotlinui.widgets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import net.globulus.kotlinui.KView
import net.globulus.kotlinui.R
import net.globulus.kotlinui.root
import net.globulus.kotlinui.traits.Data
import net.globulus.kotlinui.traits.DataProducer
import net.globulus.kotlinui.traits.FabContainer

typealias TabTitles = Array<Int>
typealias TabRenderer<D> = KView<CoordinatorLayout>.(D) -> KView<*>

class KTabs<D>(
    context: Context,
    fm: FragmentManager,
    tabTitles: TabTitles,
    dataProducer: DataProducer<D>,
    renderer: TabRenderer<D>
) : KView<CoordinatorLayout>(context), FabContainer {

  val appBarLayout: AppBarLayout
  val toolbar: Toolbar
  val tabs: TabLayout
  val viewPager: ViewPager
  override val floatingActionButton: FloatingActionButton

  override val view = (LayoutInflater.from(context).inflate(R.layout.layout_tabs, null) as CoordinatorLayout).apply {
    appBarLayout = findViewById(R.id.appBarLayout)
    toolbar = findViewById(R.id.toolbar)
    tabs = findViewById(R.id.tabs)
    viewPager = findViewById(R.id.viewPager)
    floatingActionButton = findViewById(R.id.fab)
  }

  init {
    val sectionsPagerAdapter = SectionsPagerAdapter(this, fm, tabTitles, dataProducer, renderer)
    viewPager.adapter = sectionsPagerAdapter
    tabs.setupWithViewPager(viewPager)
  }

  private class SectionsPagerAdapter<D>(
      private val owner: KTabs<D>,
      fm: FragmentManager,
      private val tabTitles: TabTitles,
      private val dataProducer: DataProducer<D>,
      private val renderer: TabRenderer<D>
  ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var data = dataProducer()

    override fun getItem(position: Int): Fragment {
      return TabFragment(owner.renderer(data[position]))
    }

    override fun getPageTitle(position: Int): CharSequence? {
     return owner.context.getString(tabTitles[position])
    }

    override fun getCount(): Int {
      return tabTitles.size
    }

    override fun notifyDataSetChanged() {
      data = dataProducer()
      super.notifyDataSetChanged()
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

fun <D> AppCompatActivity.setContentTabs(
    tabTitles: TabTitles,
    dataProducer: DataProducer<D>,
    renderer: TabRenderer<D>
): KTabs<D> {
  val tabs = KTabs(this, supportFragmentManager, tabTitles, dataProducer, renderer)
  setContentView(root(tabs).view)
  return tabs
}

fun <D> AppCompatActivity.setContentTabs(
    tabTitles: TabTitles,
    data: Data<D>,
    renderer: TabRenderer<D>) = setContentTabs(tabTitles, { data }, renderer)