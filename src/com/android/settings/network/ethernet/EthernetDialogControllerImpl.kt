/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.network.ethernet

import android.content.Context
import android.net.InetAddresses
import android.net.IpConfiguration
import android.net.LinkAddress
import android.net.StaticIpConfiguration
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.android.net.module.util.NetUtils
import com.android.settings.R
import java.net.Inet4Address
import java.net.InetAddress

class EthernetDialogControllerImpl(
    private val context: Context,
    private val ipConfiguration: IpConfiguration,
    private val parentView: View,
    private val dialog: EthernetDialog,
) : AdapterView.OnItemSelectedListener, EthernetDialogController {

    private val DHCP = 0
    private val STATIC_IP = 1

    private lateinit var mIpSettingsSpinner: Spinner
    private lateinit var mIpAddressView: TextView
    private lateinit var mGatewayView: TextView
    private lateinit var mNetworkPrefixLengthView: TextView
    private lateinit var mDns1View: TextView
    private lateinit var mDns2View: TextView

    private var mStaticIpConfiguration = StaticIpConfiguration()

    init {
        initEthernetDialog()
    }

    private fun initEthernetDialog() {
        mIpSettingsSpinner = parentView.findViewById(R.id.ip_settings)
        mIpSettingsSpinner.setOnItemSelectedListener(this)

        mIpAddressView = parentView.findViewById<TextView>(R.id.ipaddress)
        mIpAddressView.addTextChangedListener(getIpConfigFieldsTextWatcher(mIpAddressView))
        mGatewayView = parentView.findViewById<TextView>(R.id.gateway)
        mGatewayView.addTextChangedListener(getIpConfigFieldsTextWatcher(mGatewayView))
        mNetworkPrefixLengthView = parentView.findViewById<TextView>(R.id.network_prefix_length)
        mNetworkPrefixLengthView.addTextChangedListener(
            getIpConfigFieldsTextWatcher(mNetworkPrefixLengthView)
        )
        mDns1View = parentView.findViewById<TextView>(R.id.dns1)
        mDns1View.addTextChangedListener(getIpConfigFieldsTextWatcher(mDns1View))
        mDns2View = parentView.findViewById<TextView>(R.id.dns2)
        mDns2View.addTextChangedListener(getIpConfigFieldsTextWatcher(mDns2View))

        if (ipConfiguration.getIpAssignment() == IpConfiguration.IpAssignment.STATIC) {
            mIpSettingsSpinner.setSelection(STATIC_IP)

            val staticIp = ipConfiguration.getStaticIpConfiguration()
            mIpAddressView.setText(staticIp?.getIpAddress()?.getAddress()?.getHostAddress())
            mNetworkPrefixLengthView.setText(
                Integer.toString(staticIp?.getIpAddress()?.getPrefixLength() ?: 0)
            )

            if (staticIp?.getGateway() != null) {
                mGatewayView.setText(staticIp?.getGateway()?.getHostAddress())
            }

            val dnsServers = staticIp?.getDnsServers()
            if (dnsServers != null && dnsServers.size > 0) {
                mDns1View.setText(dnsServers.get(0).getHostAddress())
                if (dnsServers.size > 1) {
                    mDns2View.setText(dnsServers.get(1).getHostAddress())
                }
            }
        } else {
            mIpSettingsSpinner.setSelection(DHCP)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parentView.findViewById<View>(R.id.ip_fields)?.setVisibility(View.VISIBLE)
        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            parentView.findViewById<View>(R.id.staticip)?.setVisibility(View.VISIBLE)
            ipConfiguration.setIpAssignment(IpConfiguration.IpAssignment.STATIC)
        } else {
            parentView.findViewById<View>(R.id.staticip)?.setVisibility(View.GONE)
            ipConfiguration.setIpAssignment(IpConfiguration.IpAssignment.DHCP)
        }
    }

    private fun getIpConfigFieldsTextWatcher(view: TextView): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // work done in afterTextChanged
            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // work done in afterTextChanged
            }

            override fun afterTextChanged(s: Editable) {
                captureIpConfiguration()
            }
        }
    }

    private fun captureIpConfiguration() {
        if (mIpSettingsSpinner.getSelectedItemPosition() == STATIC_IP) {
            val ipAddr = mIpAddressView.text.toString()
            if (ipAddr.isEmpty()) return

            val inetAddr = getIPv4Address(ipAddr)
            if (inetAddr == null || inetAddr == Inet4Address.ANY) {
                return
            }

            val staticIPBuilder = StaticIpConfiguration.Builder()

            try {
                var networkPrefixLength = -1
                try {
                    networkPrefixLength = mNetworkPrefixLengthView.text.toString().toInt()
                    if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                        return
                    }
                    staticIPBuilder.setIpAddress(LinkAddress(inetAddr, networkPrefixLength))
                } catch (e: NumberFormatException) {
                    mNetworkPrefixLengthView.setText(
                        context.getString(R.string.ethernet_network_prefix_length_hint)
                    )
                } catch (e: IllegalArgumentException) {
                    return
                }

                val gateway = mGatewayView.text.toString()
                if (TextUtils.isEmpty(gateway)) {
                    try {
                        val netPart = NetUtils.getNetworkPart(inetAddr, networkPrefixLength)
                        val addr = netPart.address
                        addr[addr.size - 1] = 1
                        mGatewayView.setText(InetAddress.getByAddress(addr).getHostAddress())
                    } catch (ee: RuntimeException) {} catch (u: java.net.UnknownHostException) {}
                } else {
                    val gatewayAddr = getIPv4Address(gateway)
                    if (gatewayAddr == null || gatewayAddr.isMulticastAddress()) {
                        return
                    }
                    staticIPBuilder.setGateway(gatewayAddr)
                }

                var dns = mDns1View.text.toString()
                var dnsAddr: InetAddress? = null
                val dnsServers = ArrayList<InetAddress>()

                if (TextUtils.isEmpty(dns)) {
                    mDns1View.setText(context.getString(R.string.ethernet_dns1_hint))
                } else {
                    dnsAddr = getIPv4Address(dns)
                    if (dnsAddr == null) {
                        return
                    }
                    dnsServers.add(dnsAddr)
                }

                if (mDns2View.length() > 0) {
                    dns = mDns2View.text.toString()
                    dnsAddr = getIPv4Address(dns)
                    if (dnsAddr == null) {
                        return
                    }
                    dnsServers.add(dnsAddr)
                }
                staticIPBuilder.setDnsServers(dnsServers)
            } finally {
                mStaticIpConfiguration = staticIPBuilder.build()
            }
        }
    }

    private fun getIPv4Address(text: String): Inet4Address? {
        return try {
            val address = InetAddresses.parseNumericAddress(text)
            if (address is Inet4Address) {
                address
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun getConfig(): IpConfiguration {
        ipConfiguration.setStaticIpConfiguration(mStaticIpConfiguration)
        return ipConfiguration
    }
}
