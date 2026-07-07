package org.ivanzaytsev.tariffanalyzer.presentation.analyzer

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ConfigStatus
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessMessagesRequest
import org.ivanzaytsev.tariffanalyzer.domain.model.analyzer.ProcessingUpdate
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ProcessMessagesUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.ProcessingStatus
import org.ivanzaytsev.tariffanalyzer.presentation.analyzer.AnalyzerContract.State
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel

class AnalyzerViewModel(
    private val loadConfigStatusUseCase: LoadConfigStatusUseCase,
    private val generateConfigUseCase: GenerateConfigUseCase,
    private val validateConfigUseCase: ValidateConfigUseCase,
    private val processMessagesUseCase: ProcessMessagesUseCase,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(),
    loggerTag = "AnalyzerViewModel",
) {

    private var processingJob: Job? = null

    init {
        onAction(Action.LoadConfigStatus)
    }

    override fun reduce(action: Action) {
        when (action) {
            Action.LoadConfigStatus -> loadConfigStatus()
            is Action.ChooseTemplatesCsv -> setState {
                it.copy(selectedTemplatesFile = action.file, error = null)
            }

            is Action.ChooseTariffCsv -> setState {
                it.copy(selectedTariffFile = action.file, error = null)
            }

            Action.GenerateConfig -> generateConfig()
            Action.ValidateConfig -> validateConfig()
            is Action.ChooseMessagesCsv -> setState {
                it.copy(selectedMessagesFile = action.file, error = null)
            }

            Action.StartProcessing -> startProcessing()
            Action.CancelProcessing -> cancelProcessing()
            Action.OpenSettings -> sendEffect(Effect.NavigateToSettings)
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

    private fun generateConfig() {
        val currentState = state.value
        val templatesFile = currentState.selectedTemplatesFile
        val tariffFile = currentState.selectedTariffFile
        if (templatesFile == null || tariffFile == null) {
            sendEffect(Effect.ShowMessage("Выберите message_templates.csv и tariff.csv"))
            return
        }

        setState { it.copy(processingStatus = ProcessingStatus.GeneratingConfig, error = null) }
        viewModelScope.launch {
            runCatching { generateConfigUseCase(templatesFile, tariffFile) }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            configStatus = ConfigStatus.Valid,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                            processingStatus = ProcessingStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Конфигурация skeleton сгенерирована"))
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось сгенерировать конфигурацию")
                }
        }
    }

    private fun validateConfig() {
        if (state.value.configPath == null) {
            sendEffect(Effect.ShowMessage("Сначала сгенерируйте или загрузите конфигурацию"))
            return
        }

        setState { it.copy(processingStatus = ProcessingStatus.ValidatingConfig, error = null) }
        viewModelScope.launch {
            runCatching { validateConfigUseCase() }
                .onSuccess { issues ->
                    setState {
                        it.copy(
                            configStatus = ConfigStatus.Valid,
                            validationIssues = issues,
                            processingStatus = ProcessingStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Skeleton-валидация завершена"))
                }
                .onFailure { throwable ->
                    handleFailure(throwable, "Не удалось проверить конфигурацию")
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
