package com.example.hirelink_2025.ui.fragments.applications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentActiveApplicationsBinding
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.ui.adapters.ApplicationAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewApplicationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReviewApplicationsFragment : Fragment() {

    private var _binding: FragmentActiveApplicationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ApplicationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveApplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sampleList = listOf(
            Application(
                jobTitle = "Desarrollador Android Senior",
                companyName = "Tech Solutions S.A.C.",
                applicationDate = "Postulado hace 3 días",
                status = "En revisión",
                logoResId = R.drawable.ic_title
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "En revisión",
                logoResId = R.drawable.ic_profile
            )
        )

        adapter = ApplicationAdapter(sampleList) { item ->
            Toast.makeText(requireContext(), "Cancelaste: ${item.jobTitle}", Toast.LENGTH_SHORT).show()
        }

        binding.applicationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.applicationsRecyclerView.adapter = adapter
    }
}