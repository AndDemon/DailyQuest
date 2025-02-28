package com.hrysenko.dailyquest.presentation.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.presentation.main.MainActivity

class AssistantFragment : Fragment() {
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant, container, false)


        webView = MainActivity.getSharedWebView(requireActivity() as MainActivity)

        webView?.parent?.let { (it as ViewGroup).removeView(webView) }

        val webViewContainer = view.findViewById<ViewGroup>(R.id.webViewContainer)
        webViewContainer.addView(webView)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.parent?.let { (it as ViewGroup).removeView(webView) }
    }
}