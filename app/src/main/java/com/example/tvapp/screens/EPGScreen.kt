//package com.example.tvapp.database
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.tvapp.models.EPGProgram
//
//@Composable
//fun EPGScreen(epgPrograms: List<EPGProgram>) {
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        Text(
//            text = "EPG Schedule",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        LazyColumn {
//            items(epgPrograms) { program ->
//                EPGItem(program)

//            }
//        }
//    }
//}
//
//@Composable
//fun EPGItem(program: EPGProgram) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
//            .padding(12.dp)
//    ) {
//        Text(text = "${program.startTime} - ${program.endTime}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
//        Text(text = program.eventName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
//        Text(text = program.eventDescription, fontSize = 12.sp, color = Color.DarkGray)
//    }
//}
