package com.caastv.tvapp.view.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountScreen(
  focusRequester: FocusRequester?=null,
  profiles: List<Profile>,
  selectedProfile: Int,
  onProfileClick: (Int) -> Unit,
  onEditProfile: () -> Unit,
  subscription: SubscriptionInfo,
  registeredMobile: String,
  onUpdateMobile: () -> Unit,
  thisDevice: DeviceInfo,
  otherDevices: List<DeviceInfo>,
  onLogout: (DeviceInfo) -> Unit,
  modifier: Modifier = Modifier
) {

  Column(
    modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(Color(0xFF121212))
      .padding(16.dp)
  ) {
    ProfilesSection(
      focusRequester=focusRequester,
      profiles = profiles,
      selectedIndex = selectedProfile,
      onProfileClick = onProfileClick,
      onEditProfile = onEditProfile
    )

    Spacer(Modifier.height(24.dp))

    SubscriptionAndDevicesSection(
      subscription    = subscription,
      registeredMobile= registeredMobile,
      onUpdateMobile  = onUpdateMobile,
      thisDevice      = thisDevice,
      otherDevices    = otherDevices,
      onLogout        =  onLogout
    )
  }
}

@Composable
fun ProfilesSection(
  focusRequester: FocusRequester?=null,
  profiles: List<Profile>,
  selectedIndex: Int,
  onProfileClick: (Int) -> Unit,
  onEditProfile: () -> Unit
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxWidth()
  ) {
    Text("Profiles", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

    Button(
      onClick = onEditProfile,
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, Color(0xFF444444)),
      colors = ButtonDefaults.buttonColors(
        Color(0xFF444444),
        contentColor   = Color.White
      ),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
      Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
      Spacer(Modifier.width(4.dp))
      Text("Edit Profile", color = Color.White)
    }
  }

  Spacer(Modifier.height(12.dp))

  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(start = 0.dp, end = 16.dp)
  ) {
    itemsIndexed(profiles) { idx, profile ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .width(72.dp)
          .clickable { onProfileClick(idx) }
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .border(
              width = if (idx == selectedIndex) 3.dp else 1.dp,
              color = if (idx == selectedIndex) Color(0xFF49FEDD) else Color(0xFF444444),
              shape = CircleShape
            )
            .background(Color.DarkGray),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(profile.avatarRes),
            contentDescription = profile.name,
            modifier = Modifier.fillMaxSize()
          )
        }
        Spacer(Modifier.height(8.dp))
        Text(profile.name, color = Color.White, fontSize = 13.sp, maxLines = 1)
      }
    }
    // “Add” button
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF444444)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("Add", color = Color.White, fontSize = 13.sp)
      }
    }
  }
}

@Composable
fun SubscriptionAndDevicesSection(
  subscription: SubscriptionInfo,
  registeredMobile: String,
  onUpdateMobile: () -> Unit,
  thisDevice: DeviceInfo,
  otherDevices: List<DeviceInfo>,
  onLogout: (DeviceInfo) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text("Subscription & Devices", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))

    // Plan row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(subscription.planName, color = Color(0xFF01D8A0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("Next payment on ${subscription.nextPaymentDate}", color = Color(0xFFAAAAAA), fontSize = 14.sp)
      }
      Text("Upgrade Plan", color = Color.White, modifier = Modifier
        .background(
          brush = Brush.horizontalGradient(
            listOf(Color(0xFF3ADCAB), Color(0xFF00A3FF))
          ),
          shape = RoundedCornerShape(10.dp)
        )
        .clickable(
          onClick = {
            subscription.onUpgrade
          }
        )
        .padding(horizontal = 16.dp, vertical = 8.dp)

      )
    }

    Spacer(Modifier.height(16.dp))

    // Mobile number row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text("Registered Mobile Number", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(5.dp))
        Text(registeredMobile, color = Color(0xFFCCCCCC), fontSize = 14.sp)
      }
      Button(
        onClick = onUpdateMobile,
        border = BorderStroke(1.dp, Color(0xFF444444)),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          Color(0xFF444444),
          contentColor   = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text("Update", color = Color.White)
      }
    }

    Spacer(Modifier.height(24.dp))

    Row {
      Box(modifier = Modifier.weight(.55f)) {
        // This Device
        DeviceListSection(
          title = "This Device",
          devices = listOf(thisDevice),
          onLogout = onLogout
        )
      }

      Spacer(Modifier.width(20.dp))

      Box(modifier = Modifier.weight(.45f)){
        // Other Devices
        DeviceListSection(
          title = "Other Devices",
          devices = otherDevices,
          onLogout = onLogout
        )
      }
    }
  }
}

@Composable
fun DeviceListSection(
  title: String,
  devices: List<DeviceInfo>,
  onLogout: (DeviceInfo) -> Unit
) {
  Column {
    Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.height(8.dp))

    devices.forEach { device ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(painterResource(id = device.iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Column {
            Text(device.name, color = Color.White, fontSize = 14.sp)
            Text("Last used: ${device.lastUsed}", color = Color(0xFFAAAAAA), fontSize = 12.sp)
          }
        }
        Button(
          onClick = { onLogout(device) },
          border = BorderStroke(1.dp, Color(0xFF444444)),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            Color(0xFF444444),
            contentColor   = Color.White
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Log Out", color = Color.White)
        }
      }
    }
  }
}

// -----
// Data models for preview/demo

data class Profile(val name: String, @DrawableRes val avatarRes: Int)
data class SubscriptionInfo(
  val planName: String,
  val nextPaymentDate: String,
  val onUpgrade: () -> Unit
)
data class DeviceInfo(val name: String, val lastUsed: String, @DrawableRes val iconRes: Int)

// For gradient button you’ll need an extension to convert Brush → Color
fun Brush.toColor(): Color = Color.Unspecified // stub; replace with your own
