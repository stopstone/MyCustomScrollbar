package com.stopstone.customscrollbar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stopstone.customscrollbar.ui.theme.CustomScrollBarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomScrollBarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScrollableListWithScrollbar()
                }
            }
        }
    }
}

@Composable
fun ScrollableListWithScrollbar() {
    val listState = rememberLazyListState()
    val items = remember { List(30) { "아이템 ${it + 1}" } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            itemsIndexed(items) { _, item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp),
                )
            }
        }

        CustomScrollbarForLazyList(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) { offsetY, height ->
            ScrollbarThumb(
                offsetY = offsetY,
                height = height,
                width = 6.dp,
            )
        }
    }
}

@Composable
fun CustomScrollbarForLazyList(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbContent: @Composable BoxScope.(thumbOffset: Dp, thumbHeight: Dp) -> Unit,
) {
    var viewportHeightPx by remember { mutableIntStateOf(0) }
    var estimatedItemHeightPx by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                viewportHeightPx = coordinates.size.height
            }
    ) {
        val layoutInfo = listState.layoutInfo
        val firstVisible = layoutInfo.visibleItemsInfo.firstOrNull()
        val totalItems = layoutInfo.totalItemsCount

        if (firstVisible != null && totalItems > 0 && viewportHeightPx > 0) {
            // 평균 아이템 높이 추정 (최초 1회)
            if (estimatedItemHeightPx == null && layoutInfo.visibleItemsInfo.isNotEmpty()) {
                estimatedItemHeightPx = layoutInfo.visibleItemsInfo
                    .map { it.size }
                    .average()
                    .toFloat()
            }

            val itemHeight = estimatedItemHeightPx ?: return
            val totalContentHeight = itemHeight * totalItems

            // 콘텐츠 높이가 뷰포트보다 작으면 스크롤바 생략
            if (totalContentHeight <= viewportHeightPx) return

            val thumbHeightPx =
                (viewportHeightPx.toFloat() * viewportHeightPx.toFloat()) / totalContentHeight
            val thumbHeightPxClamped = thumbHeightPx.coerceIn(30f, viewportHeightPx.toFloat())

            val scrollOffsetY = firstVisible.index * itemHeight - firstVisible.offset
            val maxOffset = (totalContentHeight - viewportHeightPx).coerceAtLeast(1f)
            val thumbOffsetPx =
                (scrollOffsetY / maxOffset) * (viewportHeightPx - thumbHeightPxClamped)

            with(LocalDensity.current) {
                val thumbOffsetDp =
                    thumbOffsetPx.coerceIn(0f, (viewportHeightPx - thumbHeightPxClamped)).toDp()
                val thumbHeightDp = thumbHeightPxClamped.toDp()
                thumbContent(thumbOffsetDp, thumbHeightDp)
            }
        }
    }
}

@Composable
fun ScrollbarThumb(
    offsetY: Dp,
    height: Dp,
    width: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
) {
    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .height(height)
            .width(width)
            .background(color)
            .clip(shape),
    )
}
