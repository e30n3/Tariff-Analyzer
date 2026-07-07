package org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.ProcessingStatus
import org.ivanzaytsev.tariffanalyzer.presentation.screen.messageanalysis.MessageAnalysisContract.State

class MessageAnalysisViewModel(
    private val loadConfigStatusUseCase: LoadConfigStatusUseCase,
    private val processMessagesUseCase: ProcessMessagesUseCase,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(),
    loggerTag = "MessageAnalysisViewModel",
) {

    private var processingJob: Job? = null

    init {
        onAction(Action.LoadConfigStatus)
    }

    override fun reduce(action: Action) {
        when (action) {
            Action.LoadConfigStatus -> loadConfigStatus()
            is Action.ChooseMessagesCsv -> setState {
                it.copy(selectedMessagesFile = action.file, error = null)
            }

            Action.StartProcessing -> startProcessing()
            Action.CancelProcessing -> cancelProcessing()
        }
    }

    private fun loadConfigStatus() {
        setState { it.copy(isLoadingConfigStatus = true, error = null) }
        viewModelScope.launch {
            runCatching { loadConfigStatusUseCase() }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            isLoadingConfigStatus = false,
                            configStatus = result.status,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                        )
                    }
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось загрузить статус конфигурации")
                }
        }
    }

    private fun startProcessing() {
        val messagesFile = state.value.selectedMessagesFile
        if (messagesFile == null) {
            sendEffect(Effect.ShowMessage("Выберите CSV-файл сообщений"))
            return
        }
        if (state.value.configStatus != ConfigStatus.Valid) {
            sendEffect(Effect.ShowMessage("Перед обработкой нужна валидная конфигурация"))
            return
        }
        if (processingJob?.isActive == true) return

        setState {
            it.copy(
                processingStatus = ProcessingStatus.Running,
                processedRows = 0,
                totalRowsHint = null,
                progressFraction = 0f,
                outputCsvPath = null,
                logPath = null,
                error = null,
            )
        }
        processingJob = viewModelScope.launch {
            runCatching {
                processMessagesUseCase(ProcessMessagesRequest(messagesFile)).collectLatest { update ->
                    when (update) {
                        is ProcessingUpdate.Progress -> setState {
                            it.copy(
                                processedRows = update.processedRows,
                                totalRowsHint = update.totalRowsHint,
                                progressFraction = update.progressFraction.coerceIn(0f, 1f),
                            )
                        }

                        is ProcessingUpdate.Completed -> setState {
                            it.copy(
                                processingStatus = ProcessingStatus.Completed,
                                processedRows = update.processedRows,
                                totalRowsHint = update.processedRows,
                                progressFraction = 1f,
                                outputCsvPath = update.outputCsvPath,
                                logPath = update.logPath,
                            )
                        }
                    }
                }
            }.onFailure { throwable ->
                if (throwable !is CancellationException) {
                    handleFailure(throwable, "Не удалось обработать файл сообщений")
                }
            }
        }
    }

    private fun cancelProcessing() {
        val job = processingJob
        if (job?.isActive != true) return
        job.cancel()
        setState {
            it.copy(
                processingStatus = ProcessingStatus.Cancelled,
                progressFraction = 0f,
            )
        }
        sendEffect(Effect.ShowMessage("Обработка отменена"))
    }

    private fun handleFailure(throwable: Throwable, fallbackMessage: String) {
        val message = throwable.message ?: fallbackMessage
        logError(throwable, message)
        setState {
            it.copy(
                isLoadingConfigStatus = false,
                processingStatus = ProcessingStatus.Idle,
                error = message,
            )
        }
        sendEffect(Effect.ShowMessage(message))
    }
}
